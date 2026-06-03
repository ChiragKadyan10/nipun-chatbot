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
public class WhatsAppMessageReceivedEvent implements Serializable {
    private String messageId;
    private String fromPhone;
    private String toPhoneId;
    private String messageType; // TEXT, VOICE, VIDEO, IMAGE, DOCUMENT
    private String content; // Text message or parsed link
    private String mediaId; // Meta media ID if binary attachment
    private Long timestamp;
}
