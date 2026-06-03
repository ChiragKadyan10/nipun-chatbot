package com.nipun.userschool.controller;

import com.nipun.shared.dto.ApiResponse;
import com.nipun.userschool.entity.School;
import com.nipun.userschool.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping
    public ResponseEntity<ApiResponse<School>> createSchool(@RequestBody School school) {
        School createdSchool = schoolService.createSchool(school);
        return ResponseEntity.ok(ApiResponse.success("School created and database schema provisioned", createdSchool));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<School>>> getAllSchools() {
        List<School> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(ApiResponse.success(schools));
    }
}
