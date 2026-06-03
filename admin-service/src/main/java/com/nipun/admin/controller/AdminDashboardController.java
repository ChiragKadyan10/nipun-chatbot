package com.nipun.admin.controller;

import com.nipun.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final WebClient webClient;

    @Value("${services.user-school.url:http://localhost:8082}")
    private String userSchoolServiceUrl;

    @Value("${services.curriculum.url:http://localhost:8083}")
    private String curriculumServiceUrl;

    @Value("${services.scheduling.url:http://localhost:8088}")
    private String schedulingServiceUrl;

    @Value("${services.analytics.url:http://localhost:8089}")
    private String analyticsServiceUrl;

    // 1. Create a School (Super Admin only)
    @PostMapping("/schools")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Object>>> createSchool(@RequestBody Object schoolPayload) {
        log.info("Aggregator: Proxying school creation request");
        return webClient.post()
                .uri(userSchoolServiceUrl + "/api/schools")
                .bodyValue(schoolPayload)
                .retrieve()
                .toEntity(Object.class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .body(ApiResponse.success("School registered successfully", entity.getBody())));
    }

    // 2. Register a Teacher (School Admin or Super Admin)
    @PostMapping("/teachers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Object>>> createTeacher(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody Object teacherPayload) {
        log.info("Aggregator: Proxying teacher creation for tenant: {}", tenantId);
        return webClient.post()
                .uri(userSchoolServiceUrl + "/api/teachers")
                .header("X-Tenant-ID", tenantId)
                .bodyValue(teacherPayload)
                .retrieve()
                .toEntity(Object.class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .body(ApiResponse.success("Teacher registered successfully", entity.getBody())));
    }

    // 3. Upload a Lesson Plan (School Admin or Super Admin)
    @PostMapping("/curriculum/upload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Object>>> uploadCurriculum(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody Object lessonPayload) {
        log.info("Aggregator: Proxying curriculum upload for tenant: {}", tenantId);
        return webClient.post()
                .uri(curriculumServiceUrl + "/api/curriculum/upload")
                .header("X-Tenant-ID", tenantId)
                .bodyValue(lessonPayload)
                .retrieve()
                .toEntity(Object.class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .body(ApiResponse.success("Lesson plan catalogued and vector indexing triggered", entity.getBody())));
    }

    // 4. Create Broadcast Trigger Schedule
    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Object>>> createReminderSchedule(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody Object schedulePayload) {
        log.info("Aggregator: Proxying schedule creation request for tenant: {}", tenantId);
        return webClient.post()
                .uri(schedulingServiceUrl + "/api/schedules")
                .header("X-Tenant-ID", tenantId)
                .bodyValue(schedulePayload)
                .retrieve()
                .toEntity(Object.class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .body(ApiResponse.success("Quartz schedule registered", entity.getBody())));
    }

    // 5. Query School Metrics
    @GetMapping("/analytics/school/{schoolId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN', 'VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Object>>> getSchoolMetrics(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID schoolId) {
        log.info("Aggregator: Proxying metrics request for school: {}", schoolId);
        return webClient.get()
                .uri(analyticsServiceUrl + "/api/analytics/school/" + schoolId)
                .header("X-Tenant-ID", tenantId)
                .retrieve()
                .toEntity(Object.class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .body(ApiResponse.success("Fetched school analytics metrics", entity.getBody())));
    }
}
