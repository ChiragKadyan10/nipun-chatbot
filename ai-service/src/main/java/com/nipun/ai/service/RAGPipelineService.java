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
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    // Index an uploaded lesson plan document
    public void indexLessonPlan(LessonPlanUploadedEvent event) {
        log.info("Processing indexing for lesson plan: {} under tenant: {}", event.getTitle(), event.getTenantId());

        try {
            // Retrieve document binary stream (for demo/test, we parse a mock text layout)
            String rawTextContent = simulateDownloadAndParse(event.getDocumentUrl(), event.getTitle());

            // Convert to LangChain4j Document
            Document document = Document.from(rawTextContent);

            // Chunk Document (500 characters chunk with 50 characters overlap)
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            List<TextSegment> segments = splitter.split(document);

            log.info("Split lesson plan into {} text segments", segments.size());

            // Enrich Segments with metadata to enable tenant boundary filtering
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

                // Embed
                Embedding embedding = embeddingModel.embed(enriched).content();
                embeddings.add(embedding);
            }

            // Store in Qdrant Vector database
            embeddingStore.addAll(embeddings, enrichedSegments);
            log.info("Successfully indexed {} embeddings in Qdrant for {}", enrichedSegments.size(), event.getTitle());

        } catch (Exception e) {
            log.error("Failed to parse or index lesson plan document for event: {}", event, e);
        }
    }

    // Retrieve context and invoke LLM
    public String askTeacherAssistant(String query, String tenantId, UUID subjectId) {
        log.info("Answering teacher assistant query: '{}' for school tenant: {}", query, tenantId);

        // Compute query embedding
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // Build metadata filters (strict security boundaries)
        Filter tenantFilter = MetadataFilterBuilder.metadataKey("tenantId").isEqualTo(tenantId);
        Filter subjectFilter = MetadataFilterBuilder.metadataKey("subjectId").isEqualTo(subjectId.toString());
        Filter combinedFilter = Filter.and(tenantFilter, subjectFilter);

        // Fetch top-3 matches
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .filter(combinedFilter)
                .maxResults(3)
                .minScore(0.5)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n---\n"));

        log.info("Found {} relevant curriculum context segments in Qdrant", matches.size());

        // Construct context-enriched Prompt
        String systemPrompt = "You are a professional educational teaching assistant. Use the following context from the school curriculum and lesson plans to answer the teacher's question accurately.\n"
                +
                "Context:\n" +
                context + "\n\n" +
                "Teacher's Question: " + query + "\n\n" +
                "Provide detailed teaching methodologies, activity suggestions, homework ideas, or conceptual guidance.";

        // Invoke GPT LLM
        Response<dev.langchain4j.data.message.AiMessage> response = chatLanguageModel.generate(
                List.of(dev.langchain4j.data.message.UserMessage.from(systemPrompt)));

        return response.content().text();
    }

    private String simulateDownloadAndParse(String documentUrl, String title) throws Exception {
        // Here, we simulate Apache Tika parsing an incoming stream (e.g. PDF/DOCX
        // bytes)
        String mockDocumentContent = "Lesson Plan Title: " + title + "\n" +
                "Topic overview: This curriculum node explains standard lesson flows, activities, and evaluation methods.\n"
                +
                "Core Objectives: Students will comprehend basic concepts, engage in interactive group activities, and write homework assignments.\n"
                +
                "Activity Details: Conduct a 15-minute group quiz where students solve real-world scenario questions.\n"
                +
                "Homework details: Solve exercises 1 through 10 in the curriculum guide.";

        try (InputStream inputStream = new ByteArrayInputStream(mockDocumentContent.getBytes(StandardCharsets.UTF_8))) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            parser.parse(inputStream, handler, metadata, context);
            return handler.toString();
        }
    }
}
