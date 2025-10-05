package com.aurionpro.app.service;

import com.aurionpro.app.dto.DailySalesReport;
import com.aurionpro.app.dto.DashboardSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface AdminDashboardService {
    DashboardSummaryResponse getDashboardSummary();
    Page<DailySalesReport> getPaginatedSalesReport(LocalDate startDate, LocalDate endDate, Pageable pageable);
}