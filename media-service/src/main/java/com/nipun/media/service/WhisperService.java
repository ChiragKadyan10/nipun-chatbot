package com.nipun.media.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class WhisperService {

    private final WebClient webClient;
    private final String apiKey;

    public WhisperService(
            WebClient.Builder webClientBuilder,
            @Value("${openai.api-key:default_key}") String apiKey) {
        this.webClient = webClientBuilder.build();
        this.apiKey = apiKey;
    }

    public Mono<String> transcribe(byte[] audioBytes, String filename) {
        log.info("Requesting OpenAI Whisper transcription for file: {}", filename);
        if ("default_key".equals(apiKey) || apiKey.isBlank()) {
            log.warn("OpenAI API Key is default/empty. Returning simulated audio transcript.");
            return Mono.just("Mocked Whisper Audio Transcript: Show me lesson plan activities for Grade 6 Mathematics.");
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        builder.part("model", "whisper-1");

        return webClient.post()
                .uri("https://api.openai.com/v1/audio/transcriptions")
                .headers(headers -> headers.setBearerAuth(apiKey))
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response != null && response.containsKey("text")) {
                        return (String) response.get("text");
                    }
                    return "";
                })
                .doOnError(error -> log.error("Error occurred while transcribing via Whisper", error));
    }
}
