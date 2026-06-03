package com.nipun.scheduling.controller;

import com.nipun.scheduling.entity.WeeklySchedule;
import com.nipun.scheduling.service.SchedulingService;
import com.nipun.shared.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    @PostMapping
    public ResponseEntity<ApiResponse<WeeklySchedule>> createSchedule(@RequestBody ScheduleCreationRequest request) {
        WeeklySchedule schedule = WeeklySchedule.builder()
                .teacherId(request.getTeacherId())
                .subjectId(request.getSubjectId())
                .lessonPlanId(request.getLessonPlanId())
                .weekNumber(request.getWeekNumber())
                .dayOfWeek(request.getDayOfWeek())
                .reminderTime(request.getReminderTime())
                .active(true)
                .build();

        WeeklySchedule saved = schedulingService.saveAndSchedule(
                schedule,
                request.getTeacherPhone(),
                request.getTeacherName(),
                request.getLessonPlanTitle()
        );

        return ResponseEntity.ok(ApiResponse.success("Reminder schedule created and registered successfully", saved));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<String>> pauseSchedule(@PathVariable UUID id) {
        schedulingService.pauseSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule trigger paused successfully", id.toString()));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<String>> resumeSchedule(@PathVariable UUID id) {
        schedulingService.resumeSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule trigger resumed successfully", id.toString()));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleCreationRequest {
        private UUID teacherId;
        private UUID subjectId;
        private UUID lessonPlanId;
        private int weekNumber;
        private String dayOfWeek;
        private java.time.LocalTime reminderTime;
        private String teacherPhone;
        private String teacherName;
        private String lessonPlanTitle;
    }
}
