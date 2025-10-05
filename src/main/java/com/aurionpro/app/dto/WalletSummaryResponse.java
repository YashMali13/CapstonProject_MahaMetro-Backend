package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class WalletSummaryResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private Instant updatedAt;
}