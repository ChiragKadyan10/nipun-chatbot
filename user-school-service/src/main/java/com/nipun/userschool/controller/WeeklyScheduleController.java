package com.nipun.userschool.controller;

import com.nipun.shared.dto.ApiResponse;
import com.nipun.userschool.entity.WeeklySchedule;
import com.nipun.userschool.service.WeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class WeeklyScheduleController {

    private final WeeklyScheduleService weeklyScheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<WeeklySchedule>> createSchedule(@RequestBody WeeklySchedule schedule) {
        WeeklySchedule savedSchedule = weeklyScheduleService.saveSchedule(schedule);
        return ResponseEntity.ok(ApiResponse.success("Weekly schedule saved successfully", savedSchedule));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getAllSchedules() {
        return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getAllSchedules()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklySchedule>> getScheduleById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getScheduleById(id)));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getSchedulesByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getSchedulesByTeacher(teacherId)));
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getSchedulesByDay(@PathVariable String dayOfWeek) {
        return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getSchedulesByDay(dayOfWeek)));
    }

    @GetMapping("/teacher/{teacherId}/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getSchedulesByTeacherAndDay(
            @PathVariable UUID teacherId,
            @PathVariable String dayOfWeek) {
        return ResponseEntity.ok(ApiResponse.success(
                weeklyScheduleService.getSchedulesByTeacherAndDay(teacherId, dayOfWeek)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklySchedule>> updateSchedule(
            @PathVariable UUID id,
            @RequestBody WeeklySchedule schedule) {
        WeeklySchedule updatedSchedule = weeklyScheduleService.updateSchedule(id, schedule);
        return ResponseEntity.ok(ApiResponse.success("Weekly schedule updated successfully", updatedSchedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable UUID id) {
        weeklyScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Weekly schedule deleted successfully", "Deleted"));
    }
}