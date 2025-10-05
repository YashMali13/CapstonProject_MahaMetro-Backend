package com.aurionpro.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RouteRequest {
    @NotBlank(message = "Route name is required.")
    @Size(max = 100, message = "Route name cannot exceed 100 characters.")
    private String name;
}