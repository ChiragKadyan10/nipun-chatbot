package com.nipun.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analytics_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "event_type", nullable = false)
    private String eventType; // e.g. MESSAGE_PROCESSED, LESSON_COMPLETED

    @Column(name = "event_data", columnDefinition = "text")
    private String eventData; // Stringified details or jsonb representation

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
