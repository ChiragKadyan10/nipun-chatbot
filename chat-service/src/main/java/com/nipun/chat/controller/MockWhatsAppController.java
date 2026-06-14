package com.nipun.chat.controller;

import com.nipun.shared.dto.ApiResponse;
import com.nipun.shared.event.WhatsAppMessageReceivedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mock-whatsapp")
@RequiredArgsConstructor
public class MockWhatsAppController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String INCOMING_TOPIC = "incoming-messages";

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<WhatsAppMessageReceivedEvent>> receiveMockMessage(
            @RequestBody MockWhatsAppRequest request) {

        WhatsAppMessageReceivedEvent event = WhatsAppMessageReceivedEvent.builder()
                .messageId(UUID.randomUUID().toString())
                .fromPhone(request.getFromPhone())
                .toPhoneId("mock-phone-number-id")
                .messageType(request.getMessageType() != null ? request.getMessageType() : "TEXT")
                .content(request.getMessage())
                .mediaId(request.getMediaId())
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(INCOMING_TOPIC, request.getFromPhone(), event);

        return ResponseEntity.ok(
                ApiResponse.success("Mock WhatsApp message published to incoming-messages topic", event)
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockWhatsAppRequest {
        private String fromPhone;
        private String message;
        private String messageType;
        private String mediaId;
    }
}