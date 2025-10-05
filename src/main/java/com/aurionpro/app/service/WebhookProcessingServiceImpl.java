package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.PaymentConfirmationRequest;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.PaymentStatus;
import com.aurionpro.app.entity.WebhookEvent;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.WebhookEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessingServiceImpl implements WebhookProcessingService {

    private final WebhookEventRepository webhookEventRepository;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @Override
    public void processRazorpayEvent(String eventId, String eventType, String payload, String signature) throws JsonProcessingException {
        log.info("Processing Razorpay event. ID: {}, Type: {}", eventId, eventType);

        if (webhookEventRepository.existsByEventId(eventId)) {
            log.warn("Webhook event with ID [{}] has already been processed. Skipping.", eventId);
            return;
        }

        WebhookEvent event = WebhookEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .payload(payload)
                .processed(false)
                .build();
        webhookEventRepository.saveAndFlush(event);

        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String orderId = paymentEntity.path("order_id").asText();

            switch (eventType) {
                case "payment.captured":
                    handlePaymentCaptured(orderId, paymentEntity, signature);
                    break;
                case "payment.failed":
                    handlePaymentFailed(orderId);
                    break;
                default:
                    log.warn("Unhandled Razorpay event type received: {}", eventType);
                    break;
            }

            event.setProcessed(true);
            webhookEventRepository.save(event);
            log.info("Successfully processed event ID [{}].", eventId);

        } catch (Exception e) {
            log.error("Failed to process event ID [{}]. Downstream service may have failed. Event will be retried by Razorpay.", eventId, e);
            throw e;
        }
    }

    private void handlePaymentCaptured(String orderId, JsonNode paymentEntity, String signature) {
        String paymentId = paymentEntity.path("id").asText();

        Payment payment = paymentRepository.findByProviderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook received for unknown orderId: " + orderId));

        PaymentConfirmationRequest confirmation = new PaymentConfirmationRequest();
        confirmation.setRazorpayOrderId(orderId);
        confirmation.setRazorpayPaymentId(paymentId);
        confirmation.setRazorpaySignature(signature);

        if (payment.getTicketType() != null) {
            log.info("Finalizing ticket booking for orderId: {}", orderId);
            ticketService.confirmBooking(confirmation);
        } else {
            log.info("Finalizing wallet recharge for orderId: {}", orderId);
            walletService.confirmWalletRecharge(confirmation);
        }
    }

    private void handlePaymentFailed(String orderId) {
        log.warn("Processing failed payment webhook for orderId: {}", orderId);
        paymentRepository.findByProviderOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.CREATED) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.info("Marked payment for orderId {} as FAILED.", orderId);
            }
        });
    }
}