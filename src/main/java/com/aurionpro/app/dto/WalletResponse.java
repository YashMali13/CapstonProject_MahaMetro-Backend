package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private List<WalletTransactionResponse> transactions;
}