package com.nipun.ai.controller;

import com.nipun.ai.service.RAGPipelineService;
import com.nipun.shared.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final RAGPipelineService ragPipelineService;

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<String>> askAssistant(@RequestBody QueryRequest request) {
        String answer = ragPipelineService.askTeacherAssistant(
                request.getQuery(),
                request.getTenantId(),
                request.getSubjectId()
        );
        return ResponseEntity.ok(ApiResponse.success("AI response generated", answer));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryRequest {
        private String query;
        private String tenantId;
        private UUID subjectId;
    }
}
