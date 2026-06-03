package com.nipun.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "chat_session_id", nullable = false)
    private UUID chatSessionId;

    @Column(nullable = false)
    private String direction; // INCOMING, OUTGOING

    @Column(name = "message_type", nullable = false)
    private String messageType; // TEXT, VOICE, VIDEO, IMAGE, DOCUMENT

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
