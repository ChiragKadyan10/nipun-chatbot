package com.nipun.curriculum.controller;

import com.nipun.curriculum.entity.LessonPlan;
import com.nipun.curriculum.service.CurriculumService;
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
    public ResponseEntity<ApiResponse<LessonPlan>> uploadLessonPlan(@RequestBody LessonPlan lessonPlan) {
        LessonPlan savedPlan = curriculumService.uploadLessonPlan(lessonPlan);
        return ResponseEntity.ok(ApiResponse.success("Lesson plan uploaded and processing scheduled", savedPlan));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<LessonPlan>>> getLessonPlansBySubject(@PathVariable UUID subjectId) {
        List<LessonPlan> plans = curriculumService.getLessonPlansBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
