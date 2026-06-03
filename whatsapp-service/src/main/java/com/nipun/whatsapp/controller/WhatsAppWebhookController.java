package com.nipun.whatsapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nipun.shared.event.WhatsAppMessageReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/whatsapp/webhook")
@Slf4j
public class WhatsAppWebhookController {

    private final String verifyToken;
    private final String appSecret;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String INCOMING_TOPIC = "incoming-messages";

    public WhatsAppWebhookController(
            @Value("${whatsapp.verify-token:nipun_token}") String verifyToken,
            @Value("${whatsapp.app-secret:nipun_app_secret}") String appSecret,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.verifyToken = verifyToken;
        this.appSecret = appSecret;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Webhook Handshake Verification
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        log.info("Mode: {}, token: {}", mode, token);
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verified successfully!");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Process Incoming WhatsApp Event
    @PostMapping
    public Mono<ResponseEntity<Void>> receiveEvent(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawBody) {

        return Mono.defer(() -> {
            // Verify Webhook Signature
            if (signature == null || !verifySignature(rawBody, signature)) {
                log.warn("Invalid webhook signature or missing header. Request rejected.");
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
            }

            try {
                JsonNode root = objectMapper.readTree(rawBody);
                JsonNode entryNode = root.path("entry").get(0);
                if (entryNode != null) {
                    JsonNode changesNode = entryNode.path("changes").get(0);
                    if (changesNode != null) {
                        JsonNode valueNode = changesNode.path("value");
                        JsonNode messagesNode = valueNode.path("messages").get(0);
                        
                        if (messagesNode != null) {
                            String fromPhone = messagesNode.path("from").asText();
                            String messageId = messagesNode.path("id").asText();
                            String type = messagesNode.path("type").asText();
                            Long timestamp = messagesNode.path("timestamp").asLong();
                            
                            String content = "";
                            String mediaId = "";

                            if ("text".equals(type)) {
                                content = messagesNode.path("text").path("body").asText();
                            } else if ("audio".equals(type) || "voice".equals(type)) {
                                mediaId = messagesNode.path("audio").path("id").asText();
                                type = "VOICE";
                            } else if ("video".equals(type)) {
                                mediaId = messagesNode.path("video").path("id").asText();
                                type = "VIDEO";
                            } else if ("image".equals(type)) {
                                mediaId = messagesNode.path("image").path("id").asText();
                                type = "IMAGE";
                            } else if ("document".equals(type)) {
                                mediaId = messagesNode.path("document").path("id").asText();
                                type = "DOCUMENT";
                                content = messagesNode.path("document").path("filename").asText();
                            }

                            WhatsAppMessageReceivedEvent event = WhatsAppMessageReceivedEvent.builder()
                                    .messageId(messageId)
                                    .fromPhone(fromPhone)
                                    .toPhoneId(valueNode.path("metadata").path("phone_number_id").asText())
                                    .messageType(type.toUpperCase())
                                    .content(content)
                                    .mediaId(mediaId)
                                    .timestamp(timestamp)
                                    .build();

                            log.info("Parsed incoming WhatsApp message {} of type {}", messageId, type);
                            kafkaTemplate.send(INCOMING_TOPIC, fromPhone, event);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing WhatsApp webhook payload", e);
            }

            return Mono.just(ResponseEntity.ok().build());
        });
    }

    private boolean verifySignature(String payload, String signatureHeader) {
        try {
            if (!signatureHeader.startsWith("sha256=")) {
                return false;
            }
            String expectedSignature = signatureHeader.substring(7); // Remove 'sha256='
            
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            
            byte[] rawHmac = sha256HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().equals(expectedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Signature verification error", e);
            return false;
        }
    }
}
