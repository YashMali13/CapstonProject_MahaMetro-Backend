package com.aurionpro.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StationRequest {
    @NotBlank(message = "Station name is required.")
    @Size(max = 100, message = "Station name cannot exceed 100 characters.")
    private String name;
    @NotBlank(message = "Station code is required.")
    @Size(max = 10, message = "Station code cannot exceed 10 characters.")
    private String code;
    @Positive(message = "Station order must be a positive number.")
    private int stationOrder;
}