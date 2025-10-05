package com.aurionpro.app.dto;

import com.aurionpro.app.entity.TicketType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class FareResponse {

    private BigDecimal singlePassengerFare; 

    private Integer passengerCount;        

    private BigDecimal totalFare;          

    private String currency;

    private TicketType ticketType;

    private Integer durationInDays;

    private Integer totalTrips;
}
