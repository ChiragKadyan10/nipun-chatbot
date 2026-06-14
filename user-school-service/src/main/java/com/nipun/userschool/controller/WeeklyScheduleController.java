package com.nipun.userschool.controller;

import com.nipun.shared.context.TenantContext;
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
    public ResponseEntity<ApiResponse<WeeklySchedule>> createSchedule(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody WeeklySchedule schedule) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            WeeklySchedule savedSchedule = weeklyScheduleService.saveSchedule(schedule);
            return ResponseEntity.ok(ApiResponse.success("Weekly schedule saved successfully", savedSchedule));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getAllSchedules(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getAllSchedules()));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklySchedule>> getScheduleById(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID id) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getScheduleById(id)));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getSchedulesByTeacher(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID teacherId) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getSchedulesByTeacher(teacherId)));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getSchedulesByDay(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable String dayOfWeek) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            return ResponseEntity.ok(ApiResponse.success(weeklyScheduleService.getSchedulesByDay(dayOfWeek)));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/teacher/{teacherId}/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<WeeklySchedule>>> getSchedulesByTeacherAndDay(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID teacherId,
            @PathVariable String dayOfWeek) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            return ResponseEntity.ok(ApiResponse.success(
                    weeklyScheduleService.getSchedulesByTeacherAndDay(teacherId, dayOfWeek)
            ));
        } finally {
            TenantContext.clear();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeeklySchedule>> updateSchedule(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID id,
            @RequestBody WeeklySchedule schedule) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            WeeklySchedule updatedSchedule = weeklyScheduleService.updateSchedule(id, schedule);
            return ResponseEntity.ok(ApiResponse.success("Weekly schedule updated successfully", updatedSchedule));
        } finally {
            TenantContext.clear();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID id) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            weeklyScheduleService.deleteSchedule(id);
            return ResponseEntity.ok(ApiResponse.success("Weekly schedule deleted successfully", "Deleted"));
        } finally {
            TenantContext.clear();
        }
    }
}