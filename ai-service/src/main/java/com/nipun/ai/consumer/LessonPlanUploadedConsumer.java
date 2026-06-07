package com.nipun.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nipun.ai.service.RAGPipelineService;
import com.nipun.shared.event.LessonPlanUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonPlanUploadedConsumer {

    private final RAGPipelineService ragPipelineService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ai-processing", groupId = "ai-processor-group")
    public void consumeLessonPlanEvent(String message) {
        try {
            LessonPlanUploadedEvent event = objectMapper.readValue(message, LessonPlanUploadedEvent.class);

            log.info("Received LessonPlanUploadedEvent for indexing: {} (Tenant: {})",
                    event.getTitle(), event.getTenantId());

            ragPipelineService.indexLessonPlan(event);

            log.info("Completed indexing for lesson plan: {}", event.getLessonPlanId());
        } catch (Exception e) {
            log.error("Failed to parse or index lesson plan via Kafka listener. Raw message: {}", message, e);
        }
    }
}