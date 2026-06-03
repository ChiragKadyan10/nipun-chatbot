package com.nipun.whatsapp.controller;

import com.nipun.shared.dto.ApiResponse;
import com.nipun.shared.event.WhatsAppMessageReceivedEvent;
import com.nipun.shared.event.WhatsAppMessageSendRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/whatsapp/mock")
@Slf4j
public class WhatsAppMockController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final List<WhatsAppMessageSendRequestEvent> outbox = Collections.synchronizedList(new ArrayList<>());

    private static final String INCOMING_TOPIC = "incoming-messages";

    public WhatsAppMockController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Trigger an incoming mock message (text or media) into Kafka stream
    @PostMapping("/trigger")
    public ApiResponse<String> triggerMockIncoming(
            @RequestParam String fromPhone,
            @RequestParam(defaultValue = "TEXT") String type,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String mediaId) {

        WhatsAppMessageReceivedEvent event = WhatsAppMessageReceivedEvent.builder()
                .messageId(UUID.randomUUID().toString())
                .fromPhone(fromPhone)
                .toPhoneId("mock-phone-id")
                .messageType(type.toUpperCase())
                .content(text != null ? text : "Mock Media Attachment")
                .mediaId(mediaId != null ? mediaId : "")
                .timestamp(System.currentTimeMillis() / 1000)
                .build();

        kafkaTemplate.send(INCOMING_TOPIC, fromPhone, event);
        log.info("Mock incoming message triggered to Kafka: {}", event);
        return ApiResponse.success("Mock message published to " + INCOMING_TOPIC, event.getMessageId());
    }

    // Record outbound WhatsApp request sent to the mock gateway
    @PostMapping("/send")
    public ApiResponse<String> recordMockOutbound(@RequestBody WhatsAppMessageSendRequestEvent requestEvent) {
        log.info("Received outbound mock send request: {}", requestEvent);
        outbox.add(requestEvent);
        return ApiResponse.success("Message recorded in mock gateway outbox", "SUCCESS");
    }

    // Get all messages sent to the mock outbox
    @GetMapping("/outbox")
    public ApiResponse<List<WhatsAppMessageSendRequestEvent>> getOutbox() {
        return ApiResponse.success("Current Mock Outbox list", new ArrayList<>(outbox));
    }

    // Clear mock outbox
    @DeleteMapping("/outbox")
    public ApiResponse<String> clearOutbox() {
        outbox.clear();
        return ApiResponse.success("Mock Outbox cleared", "CLEARED");
    }
}
