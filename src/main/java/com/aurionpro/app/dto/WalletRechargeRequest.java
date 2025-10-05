package com.aurionpro.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletRechargeRequest {
    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "10.00", message = "Recharge amount must be at least 10.00")
    private BigDecimal amount;
}