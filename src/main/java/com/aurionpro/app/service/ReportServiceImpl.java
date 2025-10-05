package com.aurionpro.app.service;

import com.aurionpro.app.dto.ActivityLogResponse;
import com.aurionpro.app.dto.SalesReportResponse;
import com.aurionpro.app.entity.ActivityLog;
import com.aurionpro.app.mapper.ActivityLogMapper;
import com.aurionpro.app.repository.ActivityLogRepository;
import com.aurionpro.app.repository.PaymentRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @Override
    public List<SalesReportResponse> generateSalesReportData(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The 'from' date cannot be after the 'to' date.");
        }
        Instant startInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Object[]> results = paymentRepository.getSalesReportData(startInstant, endInstant);

        List<SalesReportResponse> responseList = new ArrayList<>();
        for (Object[] result : results) {
            Date date = (Date) result[0];
            LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
            BigDecimal grossRevenue = (BigDecimal) result[1];
            BigDecimal totalRefunds = (BigDecimal) result[2];
            BigDecimal netRevenue = (BigDecimal) result[3];
            responseList.add(new SalesReportResponse(localDate, grossRevenue, totalRefunds, netRevenue));
        }
        return responseList;
    }

    @Override
    public byte[] generateSalesReportFile(LocalDate from, LocalDate to, String format) throws IOException {
        List<SalesReportResponse> data = generateSalesReportData(from, to);

        if ("excel".equalsIgnoreCase(format)) {
            return createSalesReportExcel(data, from, to);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return createSalesReportPdf(data, from, to);
        } else {
            throw new IllegalArgumentException("Unsupported report format: " + format);
        }
    }

    @Override
    public byte[] generateActivityLogReport(LocalDate from, LocalDate to, String format) throws IOException {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The 'from' date cannot be after the 'to' date.");
        }
        Instant startInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<ActivityLog> data = activityLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startInstant, endInstant);

        if ("excel".equalsIgnoreCase(format)) {
            return createActivityLogExcel(data, from, to);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return createActivityLogPdf(data, from, to);
        } else {
            throw new IllegalArgumentException("Unsupported report format: " + format);
        }
    }

    @Override
    public Page<ActivityLogResponse> getActivityLog(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(activityLogMapper::toResponse);
    }

    private byte[] createSalesReportExcel(List<SalesReportResponse> data, LocalDate from, LocalDate to) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sales Report");
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Metro System Sales Report");
            titleCell.setCellStyle(titleStyle);
            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Report Period: " + from + " to " + to);
            BigDecimal totalNetRevenue = data.stream().map(SalesReportResponse::getNetRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
            Row revenueRow = sheet.createRow(3);
            revenueRow.createCell(0).setCellValue("Total Net Revenue:");
            revenueRow.createCell(1).setCellValue("₹ " + totalNetRevenue.toString());
            Row headerRow = sheet.createRow(5);
            String[] headers = {"Date", "Gross Revenue", "Total Refunds", "Net Revenue"};
            for(int i=0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowIdx = 6;
            for (SalesReportResponse reportRow : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(reportRow.getDate().toString());
                row.createCell(1).setCellValue(reportRow.getGrossRevenue().doubleValue());
                row.createCell(2).setCellValue(reportRow.getTotalRefunds().doubleValue());
                row.createCell(3).setCellValue(reportRow.getNetRevenue().doubleValue());
            }
            for(int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createSalesReportPdf(List<SalesReportResponse> data, LocalDate from, LocalDate to) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Metro System Sales Report").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Period: " + from + " to " + to).setTextAlignment(TextAlignment.CENTER));
            BigDecimal totalNetRevenue = data.stream().map(SalesReportResponse::getNetRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
            document.add(new Paragraph("Total Net Revenue: ₹" + totalNetRevenue.toString()).setBold().setFontSize(14));
            document.add(new Paragraph("\n"));
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Date")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Gross Revenue")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Total Refunds")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Net Revenue")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            for (SalesReportResponse reportRow : data) {
                table.addCell(reportRow.getDate().toString());
                table.addCell("₹" + reportRow.getGrossRevenue().toString());
                table.addCell("₹" + reportRow.getTotalRefunds().toString());
                table.addCell("₹" + reportRow.getNetRevenue().toString());
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }
    
    private byte[] createActivityLogExcel(List<ActivityLog> data, LocalDate from, LocalDate to) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Activity Log");
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("System Activity Log");
            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Report Period: " + from + " to " + to);
            Row headerRow = sheet.createRow(3);

            String[] headers = {"Timestamp (UTC)", "Actor Email", "IP Address", "Action", "Reference ID", "Details", "User Agent"};
            for(int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            int rowIdx = 4;
            for (ActivityLog log : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(log.getCreatedAt().toString());
                row.createCell(1).setCellValue(log.getActorEmail());
                row.createCell(2).setCellValue(log.getIpAddress());
                row.createCell(3).setCellValue(log.getEvent().name());
                row.createCell(4).setCellValue(log.getReferenceId());
                row.createCell(5).setCellValue(log.getDetails());
                row.createCell(6).setCellValue(log.getUserAgent());
            }
            for(int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createActivityLogPdf(List<ActivityLog> data, LocalDate from, LocalDate to) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            document.add(new Paragraph("System Activity Log").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Period: " + from + " to " + to).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 3, 2, 2, 2, 5, 5}));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Timestamp (UTC)")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Actor")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("IP Address")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Action")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Ref ID")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Details")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("User Agent")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            for (ActivityLog log : data) {
                table.addCell(new Paragraph(log.getCreatedAt().toString()).setFontSize(8));
                table.addCell(new Paragraph(log.getActorEmail()).setFontSize(8));
                table.addCell(new Paragraph(log.getIpAddress() != null ? log.getIpAddress() : "").setFontSize(8));
                table.addCell(new Paragraph(log.getEvent().name()).setFontSize(8));
                table.addCell(new Paragraph(log.getReferenceId() != null ? log.getReferenceId() : "").setFontSize(8));
                table.addCell(new Paragraph(log.getDetails() != null ? log.getDetails() : "").setFontSize(8));
                table.addCell(new Paragraph(log.getUserAgent() != null ? log.getUserAgent() : "").setFontSize(8));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }
}

