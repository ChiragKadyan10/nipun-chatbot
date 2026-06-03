package com.nipun.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanUploadedEvent implements Serializable {
    private UUID lessonPlanId;
    private UUID subjectId;
    private String tenantId;
    private String title;
    private String documentUrl;
}
