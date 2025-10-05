package com.aurionpro.app.controller;

import com.aurionpro.app.service.RazorpayService;
import com.aurionpro.app.service.WebhookProcessingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookProcessingService processingService;
    private final RazorpayService razorpayService;
    private final ObjectMapper objectMapper;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestHeader(value = "X-Razorpay-Signature") String signatureHeader,
            @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventIdHeader,
            @RequestBody String payload
    ) {
        log.info("Received Razorpay webhook.");
        if (!StringUtils.hasText(signatureHeader)) {
            log.warn("Webhook received without signature header.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature header.");
        }

        if (!razorpayService.verifyWebhookSignature(payload, signatureHeader)) {
            log.error("CRITICAL: Razorpay webhook signature verification FAILED.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature.");
        }
        log.info("Razorpay webhook signature verified successfully.");

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event").asText(null);

            String eventId = (eventIdHeader != null) ? eventIdHeader : root.path("id").asText(null);

            if (eventId == null || eventType == null) {
                log.error("Webhook payload is missing a unique event identifier or event type.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload structure.");
            }
            
            processingService.processRazorpayEvent(eventId, eventType, payload, signatureHeader);

            return ResponseEntity.ok("Webhook processed successfully.");
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook payload.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }
}