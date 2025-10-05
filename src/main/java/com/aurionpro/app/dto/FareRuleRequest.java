package com.aurionpro.app.dto;

import com.aurionpro.app.entity.TicketType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareRuleRequest {
    @NotNull(message = "Ticket type is required.")
    private TicketType ticketType;
    @Positive(message = "Min station count must be positive.")
    private Integer minStationCount;
    @Positive(message = "Max station count must be positive.")
    private Integer maxStationCount;
    @Positive(message = "Duration in days must be positive.")
    private Integer durationInDays;
    @Positive(message = "Total trips must be positive.")
    private Integer totalTrips;
    @NotNull(message = "Fare is required.")
    @Positive(message = "Fare must be a positive value.")
    private BigDecimal fare;
}