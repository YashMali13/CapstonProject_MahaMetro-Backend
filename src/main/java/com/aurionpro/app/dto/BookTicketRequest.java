package com.aurionpro.app.dto;

import com.aurionpro.app.entity.PaymentMethod;
import com.aurionpro.app.entity.TicketType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookTicketRequest {
    @NotNull(message = "Ticket type is required.")
    private TicketType ticketType;

    private Long originStationId;
    
    private Long destinationStationId;
    
    @NotNull(message = "Payment method is required.")
    private PaymentMethod paymentMethod;

    @Min(value = 1, message = "At least one passenger is required.")
    @Max(value = 5, message = "Cannot book for more than 5 passengers at a time.")
    private Integer passengerCount; 
}