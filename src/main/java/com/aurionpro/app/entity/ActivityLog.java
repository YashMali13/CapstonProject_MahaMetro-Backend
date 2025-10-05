package com.aurionpro.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String actorEmail;

    private String ipAddress; 

    @Column(length = 512)
    private String userAgent; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType event;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String entityType;
    
    private Long entityId;
    
    private String referenceId;

    @Enumerated(EnumType.STRING)
    private LogLevel level;

    public enum LogLevel {
        INFO, WARN, ERROR
    }
}