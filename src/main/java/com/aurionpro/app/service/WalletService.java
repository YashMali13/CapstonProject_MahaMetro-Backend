package com.aurionpro.app.service;

import com.aurionpro.app.dto.*;
import com.aurionpro.app.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface WalletService {

    WalletSummaryResponse getWalletForUser(String userEmail);

    Page<WalletTransactionResponse> getWalletTransactions(String userEmail, Pageable pageable);

    BookingInitiationResponse initiateWalletRecharge(WalletRechargeRequest request, String userEmail);
    
    WalletResponse confirmWalletRecharge(PaymentConfirmationRequest confirmationRequest);
    
    void processRefundForPayment(Payment payment);
    
    void processPartialRefund(Payment payment, BigDecimal refundAmount);
}