package com.nipun.userschool.controller;

import com.nipun.shared.context.TenantContext;
import com.nipun.shared.dto.ApiResponse;
import com.nipun.userschool.entity.Teacher;
import com.nipun.userschool.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<ApiResponse<Teacher>> createTeacher(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody Teacher teacher) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            Teacher savedTeacher = teacherService.saveTeacher(teacher);
            return ResponseEntity.ok(ApiResponse.success("Teacher saved successfully", savedTeacher));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Teacher>>> getAllTeachers(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            return ResponseEntity.ok(ApiResponse.success(teachers));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Teacher>> getTeacherById(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable UUID id) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            Teacher teacher = teacherService.getTeacherById(id);
            return ResponseEntity.ok(ApiResponse.success(teacher));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<ApiResponse<Teacher>> getTeacherByPhoneNumber(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @PathVariable String phoneNumber) {

        TenantContext.setTenantId(tenantId != null ? tenantId : TenantContext.DEFAULT_TENANT);

        try {
            Teacher teacher = teacherService.getTeacherByPhoneNumber(phoneNumber);
            if (teacher == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(teacher));
        } finally {
            TenantContext.clear();
        }
    }
}