package com.aurionpro.app.service;

import com.aurionpro.app.dto.DailySalesReport;
import com.aurionpro.app.dto.DashboardSummaryResponse;
import com.aurionpro.app.entity.TicketStatus;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // <-- CORRECT IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);

        BigDecimal revenueToday = paymentRepository.findTotalRevenueSince(startOfDay);
        long ticketsSoldToday = ticketRepository.countByCreatedAtAfter(startOfDay);
        long newUsersToday = userRepository.countByCreatedAtAfter(startOfDay);
        long liveJourneys = ticketRepository.countByStatus(TicketStatus.IN_TRANSIT);

        return DashboardSummaryResponse.builder()
                .revenueToday(revenueToday)
                .ticketsSoldToday(ticketsSoldToday)
                .newUsersToday(newUsersToday)
                .liveJourneys(liveJourneys)
                .build();
    }

    @Override
    public Page<DailySalesReport> getPaginatedSalesReport(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return paymentRepository.getPaginatedSalesReport(startInstant, endInstant, pageable);
    }
}