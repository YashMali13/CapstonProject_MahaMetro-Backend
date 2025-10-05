package com.aurionpro.app.service;

import com.aurionpro.app.dto.TransactionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionHistoryService {
    Page<TransactionHistoryResponse> getTransactionHistory(String userEmail, Pageable pageable);
}