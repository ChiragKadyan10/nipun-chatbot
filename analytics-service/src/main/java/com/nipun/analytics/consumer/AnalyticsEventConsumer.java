package com.nipun.analytics.consumer;

import com.nipun.analytics.entity.AnalyticsRecord;
import com.nipun.analytics.repository.AnalyticsRecordRepository;
import com.nipun.shared.context.TenantContext;
import com.nipun.shared.event.AnalyticsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final AnalyticsRecordRepository analyticsRecordRepository;

    @KafkaListener(topics = "analytics-events", groupId = "analytics-processor-group")
    public void consumeAnalyticsEvent(AnalyticsEvent event) {
        log.info("Received AnalyticsEvent of type: {} for teacher: {}", event.getEventType(), event.getTeacherId());

        TenantContext.setTenantId(event.getTenantId());
        try {
            AnalyticsRecord record = AnalyticsRecord.builder()
                    .id(UUID.randomUUID())
                    .schoolId(event.getSchoolId())
                    .teacherId(event.getTeacherId())
                    .eventType(event.getEventType())
                    .eventData("{}")
                    .timestamp(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(event.getTimestamp()), 
                            ZoneId.systemDefault()
                    ))
                    .build();

            analyticsRecordRepository.save(record);
            log.info("Successfully recorded metrics event in tenant database: {}", event.getTenantId());
        } finally {
            TenantContext.clear();
        }
    }
}
