package com.aurionpro.app.controller;

import com.aurionpro.app.dto.ScanRequest;
import com.aurionpro.app.dto.ScanResponse;
import com.aurionpro.app.dto.ValidationHistoryResponse;
import com.aurionpro.app.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<ScanResponse> scanTicket(@Valid @RequestBody ScanRequest request, Authentication authentication) {
        String staffEmail = authentication.getName();
        return ResponseEntity.ok(validationService.validateTicket(request, staffEmail));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ROLE_STAFF')")
    public ResponseEntity<List<ValidationHistoryResponse>> getHistory(@RequestParam Long ticketId) {
        return ResponseEntity.ok(validationService.getValidationHistory(ticketId));
    }
}