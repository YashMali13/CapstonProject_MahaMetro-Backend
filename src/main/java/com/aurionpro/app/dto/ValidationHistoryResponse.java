package com.aurionpro.app.dto;

import com.aurionpro.app.entity.ScanType;
import lombok.Data;
import java.time.Instant;

@Data
public class ValidationHistoryResponse {
    private Long validationId;
    private Instant scanTime;
    private String stationName;
    private Long staffMemberId;
    private ScanType scanType;
}