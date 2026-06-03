package com.nipun.whatsapp.service;

import com.nipun.shared.event.WhatsAppMessageSendRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class WhatsAppMessageSender {

    private final WebClient webClient;
    private final boolean isMock;
    private final String graphApiToken;
    private final String phoneNumberId;
    private final String serverPort;

    public WhatsAppMessageSender(
            WebClient.Builder webClientBuilder,
            @Value("${whatsapp.mock:true}") boolean isMock,
            @Value("${whatsapp.token:}") String graphApiToken,
            @Value("${whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${server.port:8084}") String serverPort) {
        this.webClient = webClientBuilder.build();
        this.isMock = isMock;
        this.graphApiToken = graphApiToken;
        this.phoneNumberId = phoneNumberId;
        this.serverPort = serverPort;
    }

    @KafkaListener(topics = "outgoing-messages", groupId = "whatsapp-sender-group")
    public void handleOutgoingMessage(WhatsAppMessageSendRequestEvent event) {
        log.info("Consuming outgoing message for recipient: {}", event.getToPhone());
        
        if (isMock) {
            sendMockMessage(event).subscribe();
        } else {
            sendMetaMessage(event).subscribe();
        }
    }

    private Mono<Void> sendMockMessage(WhatsAppMessageSendRequestEvent event) {
        log.info("Sending mock WhatsApp message to outbox: recipient={}", event.getToPhone());
        String targetUrl = "http://localhost:" + serverPort + "/api/whatsapp/mock/send";
        return webClient.post()
                .uri(targetUrl)
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("Failed to forward message to mock outbox", e);
                    return Mono.empty();
                });
    }

    private Mono<Void> sendMetaMessage(WhatsAppMessageSendRequestEvent event) {
        log.info("Sending message via Meta Graph API: recipient={}", event.getToPhone());
        String url = "https://graph.facebook.com/v19.0/" + phoneNumberId + "/messages";

        // Build Meta WhatsApp API payload
        Map<String, Object> body;
        if ("TEXT".equalsIgnoreCase(event.getMessageType())) {
            body = Map.of(
                    "messaging_product", "whatsapp",
                    "recipient_type", "individual",
                    "to", event.getToPhone(),
                    "type", "text",
                    "text", Map.of("preview_url", false, "body", event.getContent())
            );
        } else {
            // Media attachment payload
            String mediaType = event.getMessageType().toLowerCase();
            body = Map.of(
                    "messaging_product", "whatsapp",
                    "recipient_type", "individual",
                    "to", event.getToPhone(),
                    "type", mediaType,
                    mediaType, Map.of("link", event.getMediaUrl())
            );
        }

        return webClient.post()
                .uri(url)
                .headers(h -> h.setBearerAuth(graphApiToken))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(res -> log.info("Meta API reply sent. Response: {}", res))
                .doOnError(err -> log.error("Error writing to Meta Graph API", err))
                .then();
    }
}
