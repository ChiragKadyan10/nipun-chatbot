package com.nipun.curriculum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lesson_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(nullable = false)
    private String title;

    @Column(name = "grade_level", nullable = false)
    private String gradeLevel;

    @Column(nullable = false)
    private String term;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "qdrant_collection_name")
    private String qdrantCollectionName;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
