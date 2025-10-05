package com.aurionpro.app.dto;

import com.aurionpro.app.entity.WalletTransactionType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class WalletTransactionResponse {
    private Long id;
    private BigDecimal amount;
    private WalletTransactionType type;
    private String description;
    private Instant createdAt;
}