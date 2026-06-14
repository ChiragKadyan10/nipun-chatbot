package com.nipun.curriculum.controller;

import com.nipun.curriculum.entity.LessonPlan;
import com.nipun.curriculum.service.CurriculumService;
import com.nipun.shared.context.TenantContext;
import com.nipun.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @PostMapping("/upload-pdf")
    public ResponseEntity<ApiResponse<LessonPlan>> uploadPdfLessonPlan(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectId") UUID subjectId,
            @RequestParam("title") String title,
            @RequestParam("gradeLevel") String gradeLevel,
            @RequestParam("term") String term,
            @RequestParam(value = "objectives", required = false) String objectives) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Uploaded file is empty"));
            }

            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            String lowerName = originalFilename != null ? originalFilename.toLowerCase() : "";

            if (!(lowerName.endsWith(".pdf") || "application/pdf".equalsIgnoreCase(contentType))) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Only PDF files are allowed"));
            }

            String safeFilename = UUID.randomUUID().toString() + "_" + (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_") : "file.pdf");

            Path uploadDir = Paths.get("uploads", "lesson-plans");
            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(safeFilename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String documentUrl = "local://uploads/lesson-plans/" + safeFilename;

            LessonPlan lessonPlan = LessonPlan.builder()
                    .subjectId(subjectId)
                    .title(title)
                    .gradeLevel(gradeLevel)
                    .term(term)
                    .objectives(objectives)
                    .documentUrl(documentUrl)
                    .qdrantCollectionName("lesson_plans")
                    .build();

            LessonPlan savedPlan = curriculumService.uploadLessonPlan(lessonPlan);
            return ResponseEntity.ok(ApiResponse.success("Lesson plan uploaded and processing scheduled", savedPlan));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to save uploaded file"));
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