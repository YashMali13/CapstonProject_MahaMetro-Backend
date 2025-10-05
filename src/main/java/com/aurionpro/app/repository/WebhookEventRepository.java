package com.aurionpro.app.repository;

import com.aurionpro.app.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    
    boolean existsByEventId(String eventId);
}