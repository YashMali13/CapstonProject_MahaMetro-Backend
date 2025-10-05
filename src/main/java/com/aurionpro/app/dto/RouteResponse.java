package com.aurionpro.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class RouteResponse {
    private Long id;
    private String name;
    private List<StationResponse> stations;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private Instant deletedAt;
}