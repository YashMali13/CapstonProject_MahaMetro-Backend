package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ValidationRepository extends JpaRepository<Validation, Long> {
    
    List<Validation> findAllByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}