package com.aurionpro.app.dto;

import com.aurionpro.app.entity.PaymentMethod;
import com.aurionpro.app.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionHistoryResponse {
    private String transactionId; 
    private String description; 
    private Instant timestamp;
    private BigDecimal amount;
    private String type; 
    private PaymentStatus status; 
    private PaymentMethod paymentMethod;
}