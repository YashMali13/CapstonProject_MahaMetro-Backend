package com.aurionpro.app.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class StationResponse {
    private Long id;
    private String name;
    private String code;
    private int stationOrder;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private Instant deletedAt;
}