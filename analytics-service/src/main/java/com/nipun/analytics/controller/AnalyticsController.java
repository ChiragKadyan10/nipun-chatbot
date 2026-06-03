package com.nipun.analytics.controller;

import com.nipun.analytics.entity.AnalyticsRecord;
import com.nipun.analytics.repository.AnalyticsRecordRepository;
import com.nipun.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsRecordRepository analyticsRecordRepository;

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<ApiResponse<List<AnalyticsRecord>>> getSchoolAnalytics(@PathVariable UUID schoolId) {
        List<AnalyticsRecord> records = analyticsRecordRepository.findBySchoolId(schoolId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/export/csv/{schoolId}")
    public ResponseEntity<byte[]> exportAnalyticsToCsv(@PathVariable UUID schoolId) {
        List<AnalyticsRecord> records = analyticsRecordRepository.findBySchoolId(schoolId);

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,SchoolID,TeacherID,EventType,Timestamp\n");

        for (AnalyticsRecord r : records) {
            csvBuilder.append(String.format("%s,%s,%s,%s,%s\n",
                    r.getId(),
                    r.getSchoolId(),
                    r.getTeacherId(),
                    r.getEventType(),
                    r.getTimestamp()
            ));
        }

        byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "analytics_report_" + schoolId + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
