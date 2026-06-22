package com.nipun.ai.service;

import com.nipun.shared.event.LessonPlanUploadedEvent;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGPipelineService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatLanguageModel;

    private static final int OCR_MIN_TEXT_LENGTH = 500;
    private static final float OCR_RENDER_DPI = 200f;

    public void indexLessonPlan(LessonPlanUploadedEvent event) {
        log.info("Processing indexing for lesson plan: {} under tenant: {}", event.getTitle(), event.getTenantId());

        try {
            String rawTextContent = parseDocumentText(event.getDocumentUrl(), event.getTitle());

            Document document = Document.from(rawTextContent);

            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            List<TextSegment> segments = splitter.split(document);

            log.info("Split lesson plan into {} text segments", segments.size());

            List<TextSegment> enrichedSegments = new ArrayList<>();
            List<Embedding> embeddings = new ArrayList<>();

            for (TextSegment segment : segments) {
                java.util.Map<String, Object> metadata = new java.util.HashMap<>();

                metadata.put("tenantId", event.getTenantId());
                metadata.put("subjectId", event.getSubjectId().toString());
                metadata.put("lessonPlanId", event.getLessonPlanId().toString());
                metadata.put("title", event.getTitle());

                TextSegment enriched = TextSegment.from(
                        segment.text(),
                        dev.langchain4j.data.document.Metadata.from(metadata));

                enrichedSegments.add(enriched);

                Embedding embedding = embeddingModel.embed(enriched).content();
                embeddings.add(embedding);
            }

            embeddingStore.addAll(embeddings, enrichedSegments);
            log.info("Successfully indexed {} embeddings in Qdrant for {}", enrichedSegments.size(), event.getTitle());

        } catch (Exception e) {
            log.error("Failed to parse or index lesson plan document for event: {}", event, e);
        }
    }

    public String askTeacherAssistant(String query, String tenantId, UUID subjectId) {
        log.info("Answering teacher assistant query: '{}' for school tenant: {}", query, tenantId);

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        Filter tenantFilter = MetadataFilterBuilder.metadataKey("tenantId").isEqualTo(tenantId);
        Filter subjectFilter = MetadataFilterBuilder.metadataKey("subjectId").isEqualTo(subjectId.toString());
        Filter combinedFilter = Filter.and(tenantFilter, subjectFilter);

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .filter(combinedFilter)
                .maxResults(3)
                .minScore(0.5)
                .build();

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        try {
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            matches = searchResult.matches();
        } catch (Exception e) {
            log.warn("Could not search Qdrant lesson_plans collection. Falling back to generic teaching assistant response.", e);
        }

        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n---\n"));

        if (context == null || context.isBlank()) {
            context = "No indexed lesson plan context is available yet for this tenant and subject. " +
                    "Provide a helpful general teaching response based on the teacher's question.";
        }

        log.info("Found {} relevant curriculum context segments in Qdrant", matches.size());

        String systemPrompt = "You are a professional educational teaching assistant. Use the following context from the school curriculum and lesson plans to answer the teacher's question accurately.\n"
                +
                "Context:\n" +
                context + "\n\n" +
                "Teacher's Question: " + query + "\n\n" +
                "Provide detailed teaching methodologies, activity suggestions, homework ideas, or conceptual guidance.";

        Response<dev.langchain4j.data.message.AiMessage> response = chatLanguageModel.generate(
                List.of(dev.langchain4j.data.message.UserMessage.from(systemPrompt)));

        return response.content().text();
    }

    private String parseDocumentText(String documentUrl, String title) {
        if (documentUrl == null || documentUrl.isBlank()) {
            log.error("Document URL is missing for lesson plan title={}. Falling back to mock content.", title);
            return getMockDocumentText(title);
        }

        final String prefix = "local://uploads/lesson-plans/";
        if (!documentUrl.startsWith(prefix)) {
            log.error("Unsupported documentUrl scheme for lesson plan title={}: {}. Falling back to mock content.", title, documentUrl);
            return getMockDocumentText(title);
        }

        String filename = documentUrl.substring(prefix.length());
        Path pdfPath = Paths.get("..", "curriculum-service", "uploads", "lesson-plans", filename).normalize();

        if (!Files.exists(pdfPath) || !Files.isRegularFile(pdfPath)) {
            log.error("PDF file not found for lesson plan title={} at path={}. Falling back to mock content.", title, pdfPath);
            return getMockDocumentText(title);
        }

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text != null && text.trim().length() >= OCR_MIN_TEXT_LENGTH) {
                log.info("Extracted {} characters from text-based PDF for lesson plan title={}", text.length(), title);
                return text;
            }

            log.warn(
                    "PDF text extraction returned only {} characters for lesson plan title={}. Trying OCR fallback.",
                    text == null ? 0 : text.trim().length(),
                    title
            );

            String ocrText = extractTextUsingOcr(document, title);

            if (ocrText != null && !ocrText.isBlank()) {
                log.info("Extracted {} characters using OCR for lesson plan title={}", ocrText.length(), title);
                return ocrText;
            }

            log.warn("OCR fallback also returned blank text for lesson plan title={}. Falling back to mock content.", title);
            return getMockDocumentText(title);

        } catch (Exception e) {
            log.error("Failed to extract PDF text for lesson plan title={}. Falling back to mock content.", title, e);
            return getMockDocumentText(title);
        }
    }

    private String extractTextUsingOcr(PDDocument document, String title) {
        try {
            Tesseract tesseract = new Tesseract();

            String tessdataPath = System.getenv("TESSDATA_PREFIX");
            if (tessdataPath != null && !tessdataPath.isBlank()) {
                tesseract.setDatapath(tessdataPath);
            }

            tesseract.setLanguage("hin+eng");

            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder extractedText = new StringBuilder();

            int pageCount = document.getNumberOfPages();
            log.info("Starting OCR for lesson plan title={} with {} pages", title, pageCount);

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, OCR_RENDER_DPI, ImageType.RGB);
                String pageText = tesseract.doOCR(image);

                if (pageText != null && !pageText.isBlank()) {
                    extractedText
                            .append("\n\n--- OCR Page ")
                            .append(pageIndex + 1)
                            .append(" ---\n")
                            .append(pageText.trim());
                }

                log.info("OCR completed for page {}/{} of lesson plan title={}", pageIndex + 1, pageCount, title);
            }

            return extractedText.toString().trim();

        } catch (Exception e) {
            log.error("OCR extraction failed for lesson plan title={}", title, e);
            return "";
        }
    }

    private String getMockDocumentText(String title) {
        return "Lesson Plan Title: " + title + "\n" +
                "Topic overview: This curriculum node explains standard lesson flows, activities, and evaluation methods.\n" +
                "Core Objectives: Students will comprehend basic concepts, engage in interactive group activities, and write homework assignments.\n" +
                "Activity Details: Conduct a 15-minute group quiz where students solve real-world scenario questions.\n" +
                "Homework details: Solve exercises 1 through 10 in the curriculum guide.";
    }
}