package com.aurionpro.app.controller;

import com.aurionpro.app.dto.TransactionHistoryResponse;
import com.aurionpro.app.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;

    @GetMapping("/history")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<TransactionHistoryResponse>> getHistory(Authentication authentication, @ParameterObject Pageable pageable) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionHistoryService.getTransactionHistory(userEmail, pageable));
    }
}