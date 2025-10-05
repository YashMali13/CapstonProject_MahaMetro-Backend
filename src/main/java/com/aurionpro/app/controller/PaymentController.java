package com.aurionpro.app.controller;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.PaymentConfirmationRequest;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.service.RazorpayService;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final RazorpayService razorpayService;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;
    private final WalletService walletService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody PaymentConfirmationRequest request) {
        log.info("Received payment confirmation for orderId: {}", request.getRazorpayOrderId());

        boolean isValid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            log.warn("Invalid payment signature for orderId: {}", request.getRazorpayOrderId());
            return ResponseEntity.status(401).body("Invalid payment signature.");
        }

        Payment payment = paymentRepository.findByProviderOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order: " + request.getRazorpayOrderId()));

        if (payment.getTicketType() != null) {
            log.info("Routing to TicketService to confirm booking for paymentId: {}", payment.getId());
            return ResponseEntity.ok(ticketService.confirmBooking(request));
        } else {
            log.info("Routing to WalletService to confirm recharge for paymentId: {}", payment.getId());
            return ResponseEntity.ok(walletService.confirmWalletRecharge(request));
        }
    }
}