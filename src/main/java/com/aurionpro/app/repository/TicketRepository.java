package com.aurionpro.app.repository;

import java.time.Instant;
import java.util.List; // <-- NEW IMPORT
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.TicketStatus;
import com.aurionpro.app.entity.User; // <-- NEW IMPORT

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdAndUser_Email(Long id, String email);
    Page<Ticket> findAllByUser_EmailOrderByCreatedAtDesc(String email, Pageable pageable);
    boolean existsByPayment_Id(Long paymentId);
    List<Ticket> findByPayment_Id(Long paymentId);
    
    // --- NEW METHOD ---
    List<Ticket> findByUser(User user);
    
long countByCreatedAtAfter(Instant startOfDay);
    
    long countByStatus(TicketStatus status);
}