package com.aurionpro.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class StationDetailResponse {
    private Long id;
    private String name;
    private String code;
    private int stationOrder;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private Instant deletedAt;
    private List<RouteSummaryResponse> routes;
}