package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.StationDetailResponse;
import com.aurionpro.app.dto.StationRequest;
import com.aurionpro.app.dto.StationResponse;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.mapper.StationMapper;
import com.aurionpro.app.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final StationMapper stationMapper;
    private final DtoMapperService dtoMapperService; 
    @Override
    public StationResponse createStation(StationRequest request) {
        log.info("Creating new station with name: {}", request.getName());
        if (stationRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalStateException("An active station with this name already exists.");
        }
        Station station = stationMapper.toEntity(request);
        Station savedStation = stationRepository.save(station);
        return stationMapper.toResponse(savedStation);
    }

    @Override
    @Transactional(readOnly = true)
    public StationResponse getActiveStationById(Long id) {
        return stationRepository.findByIdAndDeletedFalse(id)
                .map(stationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Active station not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StationResponse> listActiveStations(Pageable pageable) {
        return stationRepository.findAllByDeletedFalse(pageable)
                .map(stationMapper::toResponse);
    }

    @Override
    public StationResponse updateStation(Long id, StationRequest request) {
        log.info("Updating station with id: {}", id);
        Station existingStation = stationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active station not found with id: " + id));
        
        if (!existingStation.getName().equalsIgnoreCase(request.getName()) && stationRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalStateException("An active station with this name already exists.");
        }

        stationMapper.updateFromDto(request, existingStation);
        Station updatedStation = stationRepository.save(existingStation);
        return stationMapper.toResponse(updatedStation);
    }

    @Override
    public void softDeleteStation(Long id) {
        log.info("Attempting to SOFT DELETE station with id: {}", id);
        Station station = stationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active station not found with id: " + id));
        
        station.setDeleted(true);
        stationRepository.save(station);
        log.info("Successfully soft-deleted station with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> listInactiveStations() {
        return stationRepository.findAllByDeletedTrue().stream()
                .map(stationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StationResponse restoreStation(Long id) {
        log.info("Restoring station with id: {}", id);
        Station inactiveStation = stationRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inactive station not found with id: " + id));
        
        inactiveStation.setDeleted(false);
        inactiveStation.setDeletedAt(null);
        Station restoredStation = stationRepository.save(inactiveStation);
        return stationMapper.toResponse(restoredStation);
    }

    @Override
    public void hardDeleteStation(Long id) {
        log.warn("Attempting to HARD DELETE station with id: {}", id);
        if (!stationRepository.existsByIdAndDeletedFalse(id)) {
            throw new ResourceNotFoundException("Cannot hard delete. Active station not found with id: " + id);
        }
        stationRepository.hardDeleteById(id);
        log.warn("Successfully hard-deleted station with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationDetailResponse> listActiveStationsWithRoutes() {
        return stationRepository.findAllWithRoutes().stream()
                .map(dtoMapperService::mapToStationDetailResponse) // <-- USE NEW SERVICE
                .collect(Collectors.toList());
    }
}