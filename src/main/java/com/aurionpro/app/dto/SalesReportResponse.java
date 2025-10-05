package com.aurionpro.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SalesReportResponse {
    
    private LocalDate date;
    private BigDecimal grossRevenue;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;

    
    public SalesReportResponse(LocalDate date, BigDecimal grossRevenue, BigDecimal totalRefunds, BigDecimal netRevenue) {
        this.date = date;
        this.grossRevenue = grossRevenue;
        this.totalRefunds = totalRefunds;
        this.netRevenue = netRevenue;
    }
}