package com.aurionpro.app.dto;

import com.aurionpro.app.entity.TicketType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class FareRuleResponse {
    private Long id;
    private TicketType ticketType;
    private Integer minStationCount;
    private Integer maxStationCount;
    private Integer durationInDays;
    private Integer totalTrips;
    private BigDecimal fare;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private Instant deletedAt;
}