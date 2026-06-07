package com.nipun.userschool.controller;

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
    public ResponseEntity<ApiResponse<Subject>> createSubject(@RequestBody Subject subject) {
        Subject savedSubject = subjectService.saveSubject(subject);
        return ResponseEntity.ok(ApiResponse.success("Subject saved successfully", savedSubject));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Subject>>> getAllSubjects() {
        List<Subject> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Subject>> getSubjectById(@PathVariable UUID id) {
        Subject subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Subject>> getSubjectByCode(@PathVariable String code) {
        Subject subject = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }
}