package com.aurionpro.app.service;

import com.aurionpro.app.dto.ActivityLogResponse;
import com.aurionpro.app.dto.SalesReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    
    List<SalesReportResponse> generateSalesReportData(LocalDate from, LocalDate to);

    byte[] generateSalesReportFile(LocalDate from, LocalDate to, String format) throws IOException;

    byte[] generateActivityLogReport(LocalDate from, LocalDate to, String format) throws IOException;
    
    Page<ActivityLogResponse> getActivityLog(Pageable pageable);
}