package com.nipun.chat.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nipun.chat.entity.ChatSession;
import com.nipun.chat.entity.Message;
import com.nipun.chat.repository.ChatSessionRepository;
import com.nipun.chat.repository.MessageRepository;
import com.nipun.shared.context.TenantContext;
import com.nipun.shared.event.MediaProcessingRequestEvent;
import com.nipun.shared.event.WhatsAppMessageReceivedEvent;
import com.nipun.shared.event.WhatsAppMessageSendRequestEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ChatProcessingConsumer {

    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String OUTGOING_TOPIC = "outgoing-messages";
    private static final String MEDIA_TOPIC = "media-processing";
    private static final String ANALYTICS_TOPIC = "analytics-events";

    @Value("${services.user-school.url:http://localhost:8082}")
    private String userSchoolServiceUrl;

    @Value("${services.ai.url:http://localhost:8086}")
    private String aiServiceUrl;

    public ChatProcessingConsumer(
        ChatSessionRepository chatSessionRepository,
        MessageRepository messageRepository,
        KafkaTemplate<String, Object> kafkaTemplate,
        RedisTemplate<String, Object> redisTemplate,
        WebClient.Builder webClientBuilder,
        ObjectMapper objectMapper) {
    this.chatSessionRepository = chatSessionRepository;
    this.messageRepository = messageRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.redisTemplate = redisTemplate;
    this.webClient = webClientBuilder.build();
    this.objectMapper = objectMapper;
}

   @KafkaListener(
        topics = "incoming-messages",
        groupId = "chat-processor-group-v3",
        containerFactory = "stringKafkaListenerContainerFactory"
)
public void processIncomingMessage(String payload) {

    WhatsAppMessageReceivedEvent event;

    try {
        event = objectMapper.readValue(payload, WhatsAppMessageReceivedEvent.class);
    } catch (Exception e) {
        log.error("Failed to parse incoming WhatsApp message payload: {}", payload, e);
        return;
    }

    log.info("Processing incoming WhatsApp message {} from phone: {}",
            event.getMessageId(),
            event.getFromPhone());

        // 1. Resolve Teacher Profile (Checking Redis Cache first)
        String cacheKey = "teacher:phone:" + event.getFromPhone();
        TeacherProfile profile = (TeacherProfile) redisTemplate.opsForValue().get(cacheKey);

        if (profile == null) {
            log.info("Redis cache miss for phone {}. Querying user-school-service...", event.getFromPhone());
            try {
                String lookupUrl = userSchoolServiceUrl + "/api/teachers/phone/" + event.getFromPhone();

                // Fetch profile synchronously
                JsonNodeResponse apiResponse = webClient.get()
                      .uri(lookupUrl)
                      .header("X-Tenant-ID", "tenant_124001")
                      .headers(headers -> headers.setBasicAuth("user", "11e49120-7ec4-4c8d-aa2d-1a69790c860e"))
                      .retrieve()
                      .bodyToMono(JsonNodeResponse.class)
                      .block(Duration.ofSeconds(10));

                if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
    profile = apiResponse.getData();

    if (profile.getTenantId() != null &&
        !profile.getTenantId().startsWith("tenant_")) {
        profile.setTenantId("tenant_" + profile.getTenantId());
    }

    // Cache in Redis for 1 Hour to reduce API call overhead
    redisTemplate.opsForValue().set(cacheKey, profile, Duration.ofHours(1));

    log.info("Successfully fetched and cached teacher profile: {}", profile.getName());
}
            } catch (Exception e) {
                log.error("Failed to query user-school-service for phone: {}", event.getFromPhone(), e);
            }
        }

        // 2. Handle Unregistered User
        if (profile == null) {
            log.warn("Phone number {} is not registered as a teacher. Sending warning reply.", event.getFromPhone());
            WhatsAppMessageSendRequestEvent reply = WhatsAppMessageSendRequestEvent.builder()
                    .toPhone(event.getFromPhone())
                    .messageType("TEXT")
                    .content(
                            "Welcome to Nipun Platform! Your phone number is not registered. Please contact your school administrator to register your account.")
                    .tenantId("public")
                    .build();
            kafkaTemplate.send(OUTGOING_TOPIC, event.getFromPhone(), reply);
            return;
        }

        // 3. Set Schema/Tenant Context dynamically
        TenantContext.setTenantId(profile.getTenantId());

        try {
            // Load or Create Chat Session
            final TeacherProfile finalProfile = profile;

            ChatSession session = chatSessionRepository
                    .findTopByTeacherIdOrderByLastActivityAtDesc(finalProfile.getId())
                    .orElseGet(() -> {
                        ChatSession newSession = ChatSession.builder()
                                .id(UUID.randomUUID())
                                .teacherId(finalProfile.getId())
                                .startedAt(LocalDateTime.now())
                                .lastActivityAt(LocalDateTime.now())
                                .currentContextNode("WELCOME")
                                .build();

                        return chatSessionRepository.save(newSession);
                    });

            // Update activity timestamp
            session.setLastActivityAt(LocalDateTime.now());
            chatSessionRepository.save(session);

            // Save incoming message log
            Message incomingMsg = Message.builder()
                    .id(UUID.randomUUID())
                    .chatSessionId(session.getId())
                    .direction("INCOMING")
                    .messageType(event.getMessageType())
                    .contentText(event.getContent())
                    .mediaUrl(event.getMediaId()) // Temp save ID
                    .timestamp(LocalDateTime.now())
                    .build();
            messageRepository.save(incomingMsg);

            // 4. Handle Media Attachments Asynchronously
            if (!"TEXT".equalsIgnoreCase(event.getMessageType())) {
                log.info("Message contains media attachment ({}). Handing off to media service.",
                        event.getMessageType());
                MediaProcessingRequestEvent mediaRequest = MediaProcessingRequestEvent.builder()
                        .mediaId(event.getMediaId())
                        .fromPhone(event.getFromPhone())
                        .messageId(event.getMessageId())
                        .messageType(event.getMessageType())
                        .tenantId(profile.getTenantId())
                        .build();

                kafkaTemplate.send(MEDIA_TOPIC, event.getFromPhone(), mediaRequest);
                return;
            }

            // 5. Query AI Assistant / RAG pipeline
            log.info("Querying AI / RAG service for teacher: {}", profile.getName());
            String aiQueryUrl = aiServiceUrl + "/api/ai/query";
            AIQueryRequest aiRequest = new AIQueryRequest(
                    event.getContent(),
                    profile.getTenantId(),
                    profile.getSubjectId());

            AIQueryResponse aiResponse = webClient.post()
                    .uri(aiQueryUrl)
                    .bodyValue(aiRequest)
                    .retrieve()
                    .bodyToMono(AIQueryResponse.class)
                    .block(Duration.ofSeconds(10));

            String aiAnswer = "Sorry, I could not retrieve details from the curriculum at this time.";
            if (aiResponse != null && aiResponse.isSuccess()) {
                aiAnswer = aiResponse.getData();
            }

            // Save outgoing message log
            Message outgoingMsg = Message.builder()
                    .id(UUID.randomUUID())
                    .chatSessionId(session.getId())
                    .direction("OUTGOING")
                    .messageType("TEXT")
                    .contentText(aiAnswer)
                    .timestamp(LocalDateTime.now())
                    .build();
            messageRepository.save(outgoingMsg);

            // 6. Broadcast reply message via WhatsApp
            WhatsAppMessageSendRequestEvent reply = WhatsAppMessageSendRequestEvent.builder()
                    .toPhone(event.getFromPhone())
                    .messageType("TEXT")
                    .content(aiAnswer)
                    .tenantId(profile.getTenantId())
                    .build();
            kafkaTemplate.send(OUTGOING_TOPIC, event.getFromPhone(), reply);
            log.info("Successfully processed chat response and dispatched to outbox topic.");

            // 7. Publish Analytics Event
            publishAnalytics(profile, event);

        } finally {
            TenantContext.clear();
        }
    }

    private void publishAnalytics(TeacherProfile profile, WhatsAppMessageReceivedEvent event) {
        // Build analytics payload to write metrics asynchronously
        // Publish to 'analytics-events'
        try {
            AnalyticsEvent analytics = new AnalyticsEvent(
                    profile.getSchoolId().toString(),
                    profile.getId().toString(),
                    "MESSAGE_PROCESSED",
                    System.currentTimeMillis());
            kafkaTemplate.send(ANALYTICS_TOPIC, profile.getSchoolId().toString(), analytics);
        } catch (Exception e) {
            log.error("Failed to post analytics metrics", e);
        }
    }

    // Helper Inner DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherProfile {
        private UUID id;
        private String name;
        private String phoneNumber;
        private String email;
        private String role;
        private UUID schoolId;
        private String tenantId;
        private UUID subjectId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonNodeResponse {
        private boolean success;
        private String message;
        private TeacherProfile data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQueryRequest {
        private String query;
        private String tenantId;
        private UUID subjectId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIQueryResponse {
        private boolean success;
        private String message;
        private String data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsEvent {
        private String schoolId;
        private String teacherId;
        private String eventType;
        private long timestamp;
    }
}
