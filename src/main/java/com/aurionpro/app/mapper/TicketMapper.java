package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.TicketResponse;
import com.aurionpro.app.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "originStation.name", target = "originStationName")
    @Mapping(source = "destinationStation.name", target = "destinationStationName")
    @Mapping(source = "payment.id", target = "paymentId")
    @Mapping(source = "payment.method", target = "paymentMethod") 
    @Mapping(source = "payment.status", target = "paymentStatus") 
    TicketResponse toResponse(Ticket ticket);
}