package com.nipun.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessingRequestEvent implements Serializable {
    private String mediaId;
    private String fromPhone;
    private String messageId;
    private String messageType; // VOICE, VIDEO, IMAGE, DOCUMENT
    private String tenantId;
    private String whatsappAccessToken;
}
