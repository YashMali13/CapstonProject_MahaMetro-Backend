package com.aurionpro.app.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    private BigDecimal revenueToday;
    private long ticketsSoldToday;
    private long newUsersToday;
    private long liveJourneys; 
}