package com.aurionpro.app.service;

import com.aurionpro.app.entity.Payment;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

import java.math.BigDecimal;

public interface RazorpayService {

    Order createOrder(BigDecimal amount) throws RazorpayException;

    boolean verifyWebhookSignature(String payload, String signature);

    /**
     * Verifies the payment signature returned to the client after a successful payment.
     * @param orderId The Razorpay Order ID.
     * @param paymentId The Razorpay Payment ID.
     * @param signature The signature provided by Razorpay to the client.
     * @return true if the signature is valid, false otherwise.
     */
    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);

    /**
     * Create a partial refund with Razorpay for a PAID payment.
     * Updates Payment.amountRefunded and sets status=REFUNDED when fully refunded.
     */
    void createRefund(Payment payment, BigDecimal amount) throws RazorpayException;
}
