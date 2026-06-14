package com.nipun.chat.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nipun.shared.event.WhatsAppMessageSendRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockWhatsAppOutboxConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "outgoing-messages",
            groupId = "mock-whatsapp-outbox-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void consumeOutgoingMessage(String payload) {
        try {
            WhatsAppMessageSendRequestEvent event =
                    objectMapper.readValue(payload, WhatsAppMessageSendRequestEvent.class);

            log.info("\n\n================ MOCK WHATSAPP SEND ================\n" +
                            "To Phone   : {}\n" +
                            "Tenant     : {}\n" +
                            "Type       : {}\n" +
                            "Message    : {}\n" +
                            "====================================================\n",
                    event.getToPhone(),
                    event.getTenantId(),
                    event.getMessageType(),
                    event.getContent()
            );

        } catch (Exception e) {
            log.error("Failed to parse outgoing WhatsApp message payload: {}", payload, e);
        }
    }
}