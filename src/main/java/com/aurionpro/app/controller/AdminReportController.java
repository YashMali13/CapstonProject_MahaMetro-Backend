package com.aurionpro.app.controller;

import com.aurionpro.app.dto.ActivityLogResponse;
import com.aurionpro.app.dto.DailySalesReport;
import com.aurionpro.app.dto.DashboardSummaryResponse;
import com.aurionpro.app.service.AdminDashboardService;
import com.aurionpro.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/admin") 
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final AdminDashboardService dashboardService;

//    @GetMapping("/dashboard/summary")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
//        return ResponseEntity.ok(dashboardService.getDashboardSummary());
//    }
//
//    @GetMapping("/reports/sales")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<Page<DailySalesReport>> getPaginatedSalesReport(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//            @ParameterObject Pageable pageable
//    ) {
//        return ResponseEntity.ok(dashboardService.getPaginatedSalesReport(startDate, endDate, pageable));
//    }
//
//    @GetMapping("/reports/sales/download")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<byte[]> downloadSalesReport(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
//            @RequestParam(defaultValue = "excel") String format
//    ) throws IOException {
//
//        byte[] reportData = reportService.generateSalesReportFile(from, to, format);
//        HttpHeaders headers = new HttpHeaders();
//        String filenameDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//
//        if ("pdf".equalsIgnoreCase(format)) {
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "sales-report-" + filenameDate + ".pdf");
//        } else {
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", "sales-report-" + filenameDate + ".xlsx");
//        }
//        return ResponseEntity.ok().headers(headers).body(reportData);
//    }
//
//    @GetMapping("/activity-log")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Page<ActivityLogResponse>> getActivityLog(@ParameterObject Pageable pageable) {
//        return ResponseEntity.ok(reportService.getActivityLog(pageable));
//    }
//
//    @GetMapping("/activity-log/download")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<byte[]> downloadActivityLogReport(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
//            @RequestParam(defaultValue = "excel") String format
//    ) throws IOException {
//
//        byte[] reportData = reportService.generateActivityLogReport(from, to, format);
//        HttpHeaders headers = new HttpHeaders();
//        String filenameDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
//
//        if ("pdf".equalsIgnoreCase(format)) {
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "activity-log-" + filenameDate + ".pdf");
//        } else {
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", "activity-log-" + filenameDate + ".xlsx");
//        }
//        return ResponseEntity.ok().headers(headers).body(reportData);
//    }

}