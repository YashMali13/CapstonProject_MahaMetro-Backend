package com.aurionpro.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SalesReportProjection {
    LocalDate getDate();
    BigDecimal getGrossRevenue();
    BigDecimal getTotalRefunds();
    BigDecimal getNetRevenue();
}