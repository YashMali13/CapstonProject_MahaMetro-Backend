package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DailySalesReport {
    private LocalDate date;
    private long ticketsSold;
    private long totalPassengers;
    private BigDecimal grossRevenue;
    private long refundsProcessed;
    private BigDecimal amountRefunded;
    private BigDecimal netRevenue;
    private Map<String, Long> ticketSalesBreakdown; 
    private Map<String, BigDecimal> revenueByPaymentMethod; 
    private long newUserRegistrations;

    public DailySalesReport(
            LocalDate date,
            Long ticketsSold,
            Long totalPassengers,
            BigDecimal grossRevenue,
            Long refundsProcessed,
            BigDecimal amountRefunded,
            BigDecimal netRevenue
    ) {
        this.date = date;
        this.ticketsSold = ticketsSold != null ? ticketsSold : 0;
        this.totalPassengers = totalPassengers != null ? totalPassengers : 0;
        this.grossRevenue = grossRevenue != null ? grossRevenue : BigDecimal.ZERO;
        this.refundsProcessed = refundsProcessed != null ? refundsProcessed : 0;
        this.amountRefunded = amountRefunded != null ? amountRefunded : BigDecimal.ZERO;
        this.netRevenue = netRevenue != null ? netRevenue : BigDecimal.ZERO;
    }
}
