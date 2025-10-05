package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.common.exception.TicketValidationException;
import com.aurionpro.app.dto.ScanRequest;
import com.aurionpro.app.dto.ScanResponse;
import com.aurionpro.app.dto.ValidationHistoryResponse;
import com.aurionpro.app.entity.*;
import com.aurionpro.app.mapper.ValidationMapper;
import com.aurionpro.app.repository.*;
import com.aurionpro.app.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    private final TicketRepository ticketRepository;
    private final ValidationRepository validationRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final JwtService jwtService;
    private final ValidationMapper validationMapper;

    @Override
    public ScanResponse validateTicket(ScanRequest request, String staffEmail) {
        log.info("Validation request received from staff [{}].", staffEmail);

        Long ticketId;
        try {
            Claims claims = jwtService.extractAllClaims(request.getQrPayload());
            ticketId = claims.get("ticketId", Long.class);
        } catch (Exception e) {
            throw new TicketValidationException("Invalid or tampered QR Code.");
        }

        User staffMember = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found: " + staffEmail));
        Station scanStation = stationRepository.findByIdAndDeletedFalse(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Scanning station not found: " + request.getStationId()));
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketValidationException("Ticket not found."));

        if (request.getScanType() == ScanType.ENTRY) {
            handleEntryScan(ticket, scanStation);
        } else {
            handleExitScan(ticket, scanStation);
        }

        ticketRepository.save(ticket);
        Validation validation = Validation.builder()
                .ticket(ticket)
                .staffMember(staffMember)
                .station(scanStation)
                .scanType(request.getScanType())
                .build();
        validationRepository.save(validation);

        log.info("Validation successful for ticket {}. New status: {}", ticket.getId(), ticket.getStatus());
        return new ScanResponse(true, "Validation Successful: " + request.getScanType().name());
    }

    private void handleEntryScan(Ticket ticket, Station scanStation) {
        if (ticket.getStatus() == TicketStatus.IN_TRANSIT) {
            throw new TicketValidationException("Ticket is already in an active journey.");
        }
        if (ticket.getStatus() != TicketStatus.NEW && ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new TicketValidationException("Ticket is not valid for entry. Status: " + ticket.getStatus());
        }

        int journeysDone = (ticket.getJourneysCompleted() == null) ? 0 : ticket.getJourneysCompleted();

        switch (ticket.getTicketType()) {
            case SINGLE:
                if (journeysDone > 0) throw new TicketValidationException("Single journey ticket already used.");
                if (!scanStation.getId().equals(ticket.getOriginStation().getId())) {
                    throw new TicketValidationException("Ticket must be used at the origin station.");
                }
                break;
            case RETURN:
                if (journeysDone == 0) {
                    if (!scanStation.getId().equals(ticket.getOriginStation().getId())) {
                        throw new TicketValidationException("First journey must start at the origin station.");
                    }
                } else if (journeysDone == 1) {
                    if (ticket.getActualExitStation() != null &&
                        !scanStation.getId().equals(ticket.getActualExitStation().getId())) {
                        throw new TicketValidationException("Return journey must start from the last exit station.");
                    }
                } else {
                    throw new TicketValidationException("Return ticket has already been fully used.");
                }
                break;
            case DAY_PASS:
                if (ticket.getValidUntil() != null && Instant.now().isAfter(ticket.getValidUntil())) {
                    ticket.setStatus(TicketStatus.EXPIRED);
                    throw new TicketValidationException("Day Pass has expired.");
                }
                validateStationIsWithinRange(ticket, scanStation);
                break;
            case MONTHLY_PASS: 
                if (ticket.getRemainingTrips() != null && ticket.getRemainingTrips() <= 0) {
                    throw new TicketValidationException("No trips remaining on this pass.");
                }
                if (ticket.getValidUntil() != null && Instant.now().isAfter(ticket.getValidUntil())) {
                    ticket.setStatus(TicketStatus.EXPIRED);
                    throw new TicketValidationException("Pass has expired.");
                }
                if (ticket.getRemainingTrips() != null) {
                    ticket.setRemainingTrips(ticket.getRemainingTrips() - 1);
                    log.info("Pass ticket {} used. Trips remaining: {}", ticket.getId(), ticket.getRemainingTrips());
                }
                break;
        }
        ticket.setStatus(TicketStatus.IN_TRANSIT);
    }

    private void handleExitScan(Ticket ticket, Station scanStation) {
        if (ticket.getStatus() != TicketStatus.IN_TRANSIT) {
            throw new TicketValidationException("Must scan for entry before exit.");
        }

        ticket.setActualExitStation(scanStation);
        int journeysDone = (ticket.getJourneysCompleted() == null) ? 0 : ticket.getJourneysCompleted();

        switch (ticket.getTicketType()) {
            case SINGLE:
                validateStationIsWithinRange(ticket, scanStation);
                ticket.setJourneysCompleted(1);
                ticket.setStatus(TicketStatus.COMPLETED);
                break;
            case RETURN:
                if (journeysDone == 0) {
                    validateStationIsWithinRange(ticket, scanStation);
                    ticket.setJourneysCompleted(1);
                    ticket.setStatus(TicketStatus.ACTIVE);
                } else {
                    if (ticket.getOriginStation() == null || !scanStation.getId().equals(ticket.getOriginStation().getId())) {
                        throw new TicketValidationException("Wrong exit station for return journey.");
                    }
                    ticket.setJourneysCompleted(2);
                    ticket.setStatus(TicketStatus.COMPLETED);
                }
                break;
            case DAY_PASS:
                if (ticket.getValidUntil() != null && Instant.now().isAfter(ticket.getValidUntil())) {
                    ticket.setStatus(TicketStatus.EXPIRED);
                    throw new TicketValidationException("Day Pass expired at exit.");
                }
                validateStationIsWithinRange(ticket, scanStation);
                ticket.setStatus(TicketStatus.ACTIVE);
                break;
            case MONTHLY_PASS: // <-- QUARTERLY_PASS removed
                if (ticket.getValidUntil() != null && Instant.now().isAfter(ticket.getValidUntil())) {
                    ticket.setStatus(TicketStatus.EXPIRED);
                    throw new TicketValidationException("Pass expired at exit.");
                }
                if (ticket.getRemainingTrips() != null && ticket.getRemainingTrips() <= 0) {
                    ticket.setStatus(TicketStatus.COMPLETED);
                } else {
                    ticket.setStatus(TicketStatus.ACTIVE);
                }
                break;
        }
    }

    private void validateStationIsWithinRange(Ticket ticket, Station scanStation) {
        if (ticket.getOriginStation() == null || ticket.getDestinationStation() == null) {
            return;
        }
        int originOrder = ticket.getOriginStation().getStationOrder();
        int destOrder = ticket.getDestinationStation().getStationOrder();
        int scanOrder = scanStation.getStationOrder();

        int minOrder = Math.min(originOrder, destOrder);
        int maxOrder = Math.max(originOrder, destOrder);

        if (scanOrder < minOrder || scanOrder > maxOrder) {
            throw new TicketValidationException("Station is outside the allowed range for this ticket.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidationHistoryResponse> getValidationHistory(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found with id: " + ticketId);
        }
        return validationRepository.findAllByTicket_IdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(validationMapper::toResponse)
                .collect(Collectors.toList());
    }
}