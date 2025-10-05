package com.aurionpro.app.repository;

import com.aurionpro.app.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

   
    Optional<PendingRegistration> findByEmail(String email);

   
    void deleteByExpiryDateBefore(Instant now);
}