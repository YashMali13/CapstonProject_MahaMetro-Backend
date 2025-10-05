package com.aurionpro.app.repository;

import com.aurionpro.app.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Finds all log entries between two timestamps, ordered from newest to oldest.
     * This is used for generating reports.
     */
    List<ActivityLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant startDate, Instant endDate);
    
    /**
     * Finds all log entries, ordered from newest to oldest, with pagination.
     * This is for displaying the log feed in the admin UI.
     */
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}