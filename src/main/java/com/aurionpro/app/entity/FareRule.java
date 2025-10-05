package com.aurionpro.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fare_rules")
@SQLDelete(sql = "UPDATE fare_rules SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    private Integer minStationCount;
    private Integer maxStationCount;

    private Integer durationInDays;
    private Integer totalTrips;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private boolean deleted = false;
    
    private Instant deletedAt;
}