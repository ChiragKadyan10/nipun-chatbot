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
public class WhatsAppMessageSendRequestEvent implements Serializable {
    private String toPhone;
    private String messageType; // TEXT, VOICE, VIDEO, IMAGE, DOCUMENT
    private String content; // Text message or template name
    private String mediaUrl; // URL of media to attach
    private String tenantId;
}
