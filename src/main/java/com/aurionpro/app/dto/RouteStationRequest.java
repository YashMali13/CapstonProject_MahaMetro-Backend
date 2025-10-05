package com.aurionpro.app.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class RouteStationRequest {
    @NotEmpty(message = "A list of station IDs is required.")
    private List<Long> stationIds;
}