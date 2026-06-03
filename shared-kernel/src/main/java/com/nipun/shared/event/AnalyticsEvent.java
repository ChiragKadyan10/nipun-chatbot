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
public class AnalyticsEvent implements Serializable {
    private UUID schoolId;
    private UUID teacherId;
    private String tenantId;
    private String eventType;
    private Long timestamp;
}
