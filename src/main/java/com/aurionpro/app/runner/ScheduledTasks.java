package com.aurionpro.app.runner;

import com.aurionpro.app.repository.PendingRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final PendingRegistrationRepository pendingRegistrationRepository;

    
    @Scheduled(fixedRateString = "${app.scheduling.cleanup-rate-ms:86400000}") 
    @Transactional
    public void cleanupPendingRegistrations() {
        log.info("Running scheduled job: Cleaning up expired pending registrations...");
        
        pendingRegistrationRepository.deleteByExpiryDateBefore(Instant.now());
        
        log.info("Pending registration cleanup complete.");
    }
}