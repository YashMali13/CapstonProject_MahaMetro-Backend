package com.aurionpro.app.dto;

import com.aurionpro.app.entity.PaymentMethod;
import com.aurionpro.app.entity.PaymentStatus;
import com.aurionpro.app.entity.TicketStatus;
import com.aurionpro.app.entity.TicketType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TicketResponse {
    private Long id;
    private Long userId;
    private String originStationName;
    private String destinationStationName;
    private Long paymentId;
    private PaymentMethod paymentMethod; 
    private PaymentStatus paymentStatus; 
    private TicketType ticketType;
    private BigDecimal fare;
    private TicketStatus status;
    private Instant validFrom;
    private Instant validUntil;
    private Integer remainingTrips;
    private Instant createdAt;

}