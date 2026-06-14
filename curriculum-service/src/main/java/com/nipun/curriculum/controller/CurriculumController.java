package com.nipun.curriculum.controller;

import com.nipun.curriculum.entity.LessonPlan;
import com.nipun.curriculum.service.CurriculumService;
import com.nipun.shared.context.TenantContext;
import com.nipun.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/curriculum")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<LessonPlan>> uploadLessonPlan(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody LessonPlan lessonPlan) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            LessonPlan savedPlan = curriculumService.uploadLessonPlan(lessonPlan);
            return ResponseEntity.ok(ApiResponse.success("Lesson plan uploaded and processing scheduled", savedPlan));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonPlan>> getLessonPlanById(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID id) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            LessonPlan plan = curriculumService.getLessonPlanById(id);
            return ResponseEntity.ok(ApiResponse.success(plan));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<LessonPlan>>> getLessonPlansBySubject(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID subjectId) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            List<LessonPlan> plans = curriculumService.getLessonPlansBySubject(subjectId);
            return ResponseEntity.ok(ApiResponse.success(plans));
        } finally {
            TenantContext.clear();
        }
    }
}