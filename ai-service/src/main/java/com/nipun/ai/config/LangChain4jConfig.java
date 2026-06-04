package com.nipun.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LangChain4jConfig {

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6334}")
    private int qdrantPort;

    @Value("${qdrant.api-key:}")
    private String qdrantApiKey;

    @Value("${openai.api-key:default_key}")
    private String openAiApiKey;

    @Bean
    public EmbeddingModel embeddingModel() {
        if ("default_key".equals(openAiApiKey) || openAiApiKey.isBlank()) {
            log.warn("OpenAI API Key is empty or default. Mocking embedding model (size=1536).");
            return new EmbeddingModel() {

                @Override
                public Response<dev.langchain4j.data.embedding.Embedding> embed(String text) {
                    float[] vector = new float[1536];
                    return Response.from(new dev.langchain4j.data.embedding.Embedding(vector));
                }

                @Override
                public Response<dev.langchain4j.data.embedding.Embedding> embed(
                        dev.langchain4j.data.segment.TextSegment textSegment) {
                    float[] vector = new float[1536];
                    return Response.from(new dev.langchain4j.data.embedding.Embedding(vector));
                }

                @Override
                public Response<java.util.List<dev.langchain4j.data.embedding.Embedding>> embedAll(
                        java.util.List<dev.langchain4j.data.segment.TextSegment> textSegments) {

                    java.util.List<dev.langchain4j.data.embedding.Embedding> embeddings = textSegments.stream()
                            .map(ts -> new dev.langchain4j.data.embedding.Embedding(new float[1536]))
                            .toList();

                    return Response.from(embeddings);
                }
            };
        }

        return OpenAiEmbeddingModel.builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("default_key".equals(openAiApiKey) || openAiApiKey.isBlank()) {
            log.warn("OpenAI API Key is empty or default. Mocking chat language model.");
            return (messages) -> Response.from(dev.langchain4j.data.message.AiMessage.from(
                    "Mock AI Teaching Assistant Response:\n" +
                            "To teach fractions to Grade 6 students:\n" +
                            "1. Introduce visual fraction models (pizza slices).\n" +
                            "2. Run a classroom game measuring liquids in containers.\n" +
                            "3. Homework: Practice adding fractions with like denominators."));
        }

        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o")
                .temperature(0.3)
                .build();
    }

    @Bean
    public EmbeddingStore<dev.langchain4j.data.segment.TextSegment> embeddingStore() {
        log.info("Connecting to Qdrant vector database at {}:{}", qdrantHost, qdrantPort);
        return QdrantEmbeddingStore.builder()
                .host(qdrantHost)
                .port(qdrantPort)
                .apiKey(qdrantApiKey)
                .collectionName("lesson_plans")
                .build();
    }
}
