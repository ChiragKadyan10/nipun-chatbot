package com.nipun.userschool.controller;

import com.nipun.shared.context.TenantContext;
import com.nipun.shared.dto.ApiResponse;
import com.nipun.userschool.entity.Subject;
import com.nipun.userschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<Subject>> createSubject(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody Subject subject) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            Subject savedSubject = subjectService.saveSubject(subject);
            return ResponseEntity.ok(ApiResponse.success("Subject saved successfully", savedSubject));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Subject>>> getAllSubjects(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            return ResponseEntity.ok(ApiResponse.success(subjects));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Subject>> getSubjectById(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID id) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            Subject subject = subjectService.getSubjectById(id);
            return ResponseEntity.ok(ApiResponse.success(subject));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Subject>> getSubjectByCode(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable String code) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            Subject subject = subjectService.getSubjectByCode(code);
            return ResponseEntity.ok(ApiResponse.success(subject));
        } finally {
            TenantContext.clear();
        }
    }
}