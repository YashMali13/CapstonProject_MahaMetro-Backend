package com.aurionpro.app.controller;

import com.aurionpro.app.dto.*;
import com.aurionpro.app.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

   
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WalletSummaryResponse> getMyWallet(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(walletService.getWalletForUser(userEmail));
    }

   
    @GetMapping("/me/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<WalletTransactionResponse>> getMyWalletTransactions(
            Authentication authentication,
            @ParameterObject Pageable pageable
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(walletService.getWalletTransactions(userEmail, pageable));
    }

   
    @PostMapping("/recharge")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingInitiationResponse> rechargeWallet(
            @Valid @RequestBody WalletRechargeRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(walletService.initiateWalletRecharge(request, userEmail));
    }

    @PostMapping("/recharge/confirm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletResponse> confirmRechargeFromClient(
            @Valid @RequestBody PaymentConfirmationRequest confirmationRequest
    ) {
        // This call re-uses the exact same robust logic your webhook uses.
        WalletResponse walletResponse = walletService.confirmWalletRecharge(confirmationRequest);
        return ResponseEntity.ok(walletResponse);
    }

}