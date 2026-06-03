package com.nipun.scheduling.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "weekly_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "lesson_plan_id", nullable = false)
    private UUID lessonPlanId;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek; // e.g. "MONDAY"

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false)
    private boolean active;
}
