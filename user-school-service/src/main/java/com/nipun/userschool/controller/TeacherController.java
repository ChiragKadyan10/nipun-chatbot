package com.nipun.userschool.controller;

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
    public ResponseEntity<ApiResponse<Teacher>> createTeacher(@RequestBody Teacher teacher) {
        Teacher savedTeacher = teacherService.saveTeacher(teacher);
        return ResponseEntity.ok(ApiResponse.success("Teacher saved successfully", savedTeacher));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Teacher>>> getAllTeachers() {
        List<Teacher> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success(teachers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Teacher>> getTeacherById(@PathVariable UUID id) {
        Teacher teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success(teacher));
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<ApiResponse<Teacher>> getTeacherByPhoneNumber(@PathVariable String phoneNumber) {
        Teacher teacher = teacherService.getTeacherByPhoneNumber(phoneNumber);
        if (teacher == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(teacher));
    }
}
