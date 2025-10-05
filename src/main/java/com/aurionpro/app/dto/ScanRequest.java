package com.aurionpro.app.dto;

import com.aurionpro.app.entity.ScanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScanRequest {
    @NotBlank(message = "QR payload cannot be empty.")
    private String qrPayload;
    @NotNull(message = "Station ID is required.")
    private Long stationId;
    @NotNull(message = "Scan type is required.")
    private ScanType scanType;
}