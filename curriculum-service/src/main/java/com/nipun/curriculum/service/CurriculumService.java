package com.nipun.curriculum.service;

import com.nipun.curriculum.entity.LessonPlan;
import com.nipun.curriculum.repository.LessonPlanRepository;
import com.nipun.shared.context.TenantContext;
import com.nipun.shared.event.LessonPlanUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumService {

    private final LessonPlanRepository lessonPlanRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String AI_PROCESSING_TOPIC = "ai-processing";

    @Transactional
    public LessonPlan uploadLessonPlan(LessonPlan lessonPlan) {
        String tenantId = TenantContext.getTenantId();
        log.info("Uploading lesson plan: {} for tenant: {}", lessonPlan.getTitle(), tenantId);

        // Save to Database
        LessonPlan savedPlan = lessonPlanRepository.save(lessonPlan);

        // Dispatch Kafka event for AI Service (to chunk, embed and index in Qdrant)
        LessonPlanUploadedEvent event = LessonPlanUploadedEvent.builder()
                .lessonPlanId(savedPlan.getId())
                .subjectId(savedPlan.getSubjectId())
                .tenantId(tenantId)
                .title(savedPlan.getTitle())
                .documentUrl(savedPlan.getDocumentUrl())
                .build();

        kafkaTemplate.send(AI_PROCESSING_TOPIC, savedPlan.getId().toString(), event);
        log.info("Dispatched LessonPlanUploadedEvent to Kafka topic: {}", AI_PROCESSING_TOPIC);

        return savedPlan;
    }

    public List<LessonPlan> getLessonPlansBySubject(UUID subjectId) {
        return lessonPlanRepository.findBySubjectId(subjectId);
    }
    public LessonPlan getLessonPlanById(UUID id) {
        return lessonPlanRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lesson plan not found: " + id));
  }

}
