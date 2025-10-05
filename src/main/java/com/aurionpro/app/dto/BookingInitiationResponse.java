package com.aurionpro.app.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BookingInitiationResponse {
    private Long internalPaymentId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;

}