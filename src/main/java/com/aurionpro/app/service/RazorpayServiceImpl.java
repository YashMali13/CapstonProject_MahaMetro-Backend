package com.aurionpro.app.service;

import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.PaymentStatus;
import com.aurionpro.app.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayClient razorpayClient;
    private final String keySecret;      
    private final String webhookSecret;  
    private final PaymentRepository paymentRepository;

    public RazorpayServiceImpl(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret,
            @Value("${razorpay.webhook.secret}") String webhookSecret,
            PaymentRepository paymentRepository
    ) throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Order createOrder(BigDecimal amount) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
        return razorpayClient.orders.create(orderRequest);
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            return Utils.verifyWebhookSignature(payload, signature, this.webhookSecret);
        } catch (RazorpayException e) {
            return false;
        }
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_order_id", orderId);
            attrs.put("razorpay_payment_id", paymentId);
            attrs.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(attrs, this.keySecret);
        } catch (RazorpayException e) {
            return false;
        }
    }

    @Override
    public void createRefund(Payment payment, BigDecimal amount) throws RazorpayException {
        if (payment.getProviderPaymentId() == null) {
            throw new IllegalStateException("Cannot refund: provider paymentId is null.");
        }
        if (payment.getStatus() != PaymentStatus.PAID && payment.getAmountRefunded() == null) {
            throw new IllegalStateException("Refund allowed only for PAID/partially refunded payments. Current: " + payment.getStatus());
        }

        BigDecimal alreadyRefunded = payment.getAmountRefunded() == null ? BigDecimal.ZERO : payment.getAmountRefunded();
        BigDecimal newTotal = alreadyRefunded.add(amount);
        if (newTotal.compareTo(payment.getAmount()) > 0) {
            throw new IllegalStateException("Refund amount exceeds original payment.");
        }

        JSONObject refundReq = new JSONObject();
        refundReq.put("amount", amount.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP));
        refundReq.put("speed", "normal");

        razorpayClient.payments.refund(payment.getProviderPaymentId(), refundReq);

        payment.setAmountRefunded(newTotal);
        if (newTotal.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        }
        paymentRepository.save(payment);
    }
}
