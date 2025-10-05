package com.aurionpro.app.repository;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.aurionpro.app.dto.DailySalesReport;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.User;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderOrderId(String providerOrderId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.providerOrderId = :providerOrderId")
    Optional<Payment> findByProviderOrderIdForUpdate(String providerOrderId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(Long id);

    @Query("SELECT FUNCTION('DATE', p.createdAt) as reportDate, " +
           "SUM(CASE WHEN p.status = 'PAID' OR p.status = 'REFUNDED' THEN p.amount ELSE 0 END), " +
           "SUM(CASE WHEN p.status = 'REFUNDED' THEN p.amount ELSE 0 END), " +
           "SUM(CASE WHEN p.status = 'PAID' THEN p.amount ELSE (p.amount * -1) END) " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "AND p.status IN ('PAID', 'REFUNDED') " +
           "GROUP BY reportDate ORDER BY reportDate ASC")
    List<Object[]> getSalesReportData(Instant startDate, Instant endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.tickets IS NOT EMPTY")
    List<Payment> findTicketPaymentsByUser(User user);
    
    @Query("SELECT new com.aurionpro.app.dto.DailySalesReport(" +
    	       "CAST(p.createdAt AS java.time.LocalDate), " +
    	       "COUNT(p), " +
    	       "SUM(p.passengerCount), " +
    	       "SUM(CASE WHEN p.status = 'PAID' THEN p.amount ELSE 0 END), " +
    	       "SUM(CASE WHEN p.status = 'REFUNDED' THEN 1 ELSE 0 END), " +
    	       "SUM(p.amountRefunded), " +
    	       "SUM(CASE WHEN p.status = 'PAID' THEN p.amount ELSE (p.amountRefunded * -1) END)) " +
    	       "FROM Payment p WHERE p.ticketType IS NOT NULL AND p.createdAt >= :startDate AND p.createdAt < :endDate " +
    	       "GROUP BY CAST(p.createdAt AS java.time.LocalDate) " +
    	       "ORDER BY CAST(p.createdAt AS java.time.LocalDate) DESC")
    	Page<DailySalesReport> getPaginatedSalesReport(Instant startDate, Instant endDate, Pageable pageable);

    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt >= :startOfDay")
    BigDecimal findTotalRevenueSince(Instant startOfDay);
}