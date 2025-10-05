package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.FareRuleNotFoundException;
import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.FareRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.entity.FareRule;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.entity.TicketType;
import com.aurionpro.app.repository.FareRuleRepository;
import com.aurionpro.app.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FareServiceImpl implements FareService {

    private final FareRuleRepository fareRuleRepository;
    private final StationRepository stationRepository;

    @Override
    public FareResponse calculateFare(FareRequest request) {
        log.info("Calculating fare for request: {}", request);

        int passengerCount = request.getPassengerCount();
        TicketType type = request.getTicketType();
        FareRule applicableRule;

        if (type == TicketType.SINGLE || type == TicketType.RETURN) {
            if (request.getOriginStationId() == null || request.getDestinationStationId() == null) {
                throw new IllegalArgumentException("Origin and Destination stations are required for this ticket type.");
            }
            applicableRule = findJourneyFareRule(request.getOriginStationId(), request.getDestinationStationId(), type);
        } else {
            applicableRule = findPassFareRule(type);
        }

        BigDecimal singleFare = applicableRule.getFare();
        BigDecimal totalFare = singleFare.multiply(new BigDecimal(passengerCount));

        return FareResponse.builder()
                .singlePassengerFare(singleFare)
                .passengerCount(passengerCount)
                .totalFare(totalFare)
                .currency("INR")
                .ticketType(type)
                .durationInDays(applicableRule.getDurationInDays())
                .totalTrips(applicableRule.getTotalTrips())
                .build();
    }

    private FareRule findJourneyFareRule(Long originId, Long destId, TicketType type) {
        Station origin = stationRepository.findByIdAndDeletedFalse(originId)
                .orElseThrow(() -> new ResourceNotFoundException("Origin station not found with id: " + originId));
        Station destination = stationRepository.findByIdAndDeletedFalse(destId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found with id: " + destId));

        int stationCount = Math.abs(destination.getStationOrder() - origin.getStationOrder());

        return fareRuleRepository.findByTicketTypeAndMinStationCountLessThanEqualAndMaxStationCountGreaterThanEqualAndDeletedFalse(
                        type, stationCount, stationCount)
                .orElseThrow(() -> new FareRuleNotFoundException(
                        String.format("No applicable fare rule found for a %s journey of %d stations.", type, stationCount))
                );
    }

    private FareRule findPassFareRule(TicketType type) {
        return fareRuleRepository.findByTicketTypeAndDeletedFalse(type)
                .orElseThrow(() -> new FareRuleNotFoundException("No applicable fare rule found for ticket type: " + type));
    }
}
