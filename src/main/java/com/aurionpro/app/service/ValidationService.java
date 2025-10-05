package com.aurionpro.app.service;

import com.aurionpro.app.dto.ScanRequest;
import com.aurionpro.app.dto.ScanResponse;
import com.aurionpro.app.dto.ValidationHistoryResponse;
import java.util.List;

public interface ValidationService {
    ScanResponse validateTicket(ScanRequest request, String staffEmail);
    List<ValidationHistoryResponse> getValidationHistory(Long ticketId);
}