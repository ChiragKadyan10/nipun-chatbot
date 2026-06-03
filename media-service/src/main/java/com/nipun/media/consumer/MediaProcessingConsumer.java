package com.nipun.media.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.nipun.media.service.MediaStorageService;
import com.nipun.media.service.WhisperService;
import com.nipun.shared.event.MediaProcessingRequestEvent;
import com.nipun.shared.event.WhatsAppMessageReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@Slf4j
public class MediaProcessingConsumer {

    private final WebClient webClient;
    private final MediaStorageService mediaStorageService;
    private final WhisperService whisperService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String INCOMING_MESSAGES_TOPIC = "incoming-messages";

    public MediaProcessingConsumer(
            WebClient.Builder webClientBuilder,
            MediaStorageService mediaStorageService,
            WhisperService whisperService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.webClient = webClientBuilder.build();
        this.mediaStorageService = mediaStorageService;
        this.whisperService = whisperService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "media-processing", groupId = "media-processor-group")
    public void processMedia(MediaProcessingRequestEvent event) {
        log.info("Consuming media processing task for message: {}, type: {}", event.getMessageId(), event.getMessageType());

        // In a live environment, fetch Meta Media details and download the file.
        // We'll mock the binary download, upload to S3, and trigger Whisper.
        
        byte[] mockAudioBytes = "mock-audio-payload".getBytes();
        String fileKey = "schools/" + event.getTenantId() + "/media/" + UUID.randomUUID() + ".ogg";
        
        // Upload to S3
        mediaStorageService.uploadFile(
                fileKey,
                new ByteArrayInputStream(mockAudioBytes),
                mockAudioBytes.length,
                "audio/ogg"
        );

        // Transcribe if it's a voice message
        if ("VOICE".equalsIgnoreCase(event.getMessageType())) {
            whisperService.transcribe(mockAudioBytes, "voice.ogg")
                    .flatMap(transcript -> {
                        log.info("Whisper Transcription Result: '{}'", transcript);

                        // Broadcast the transcribed text back into the normal text processing flow
                        WhatsAppMessageReceivedEvent textEvent = WhatsAppMessageReceivedEvent.builder()
                                .messageId(event.getMessageId())
                                .fromPhone(event.getFromPhone())
                                .toPhoneId("transcribed-gateway")
                                .messageType("TEXT")
                                .content(transcript) // Transcribed text
                                .mediaId("")
                                .timestamp(System.currentTimeMillis() / 1000)
                                .build();

                        kafkaTemplate.send(INCOMING_MESSAGES_TOPIC, event.getFromPhone(), textEvent);
                        log.info("Re-dispatched voice message as text event to: {}", INCOMING_MESSAGES_TOPIC);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }
}
