package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.*;
import com.aurionpro.app.entity.Route;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.mapper.RouteMapper;
import com.aurionpro.app.mapper.StationMapper;
import com.aurionpro.app.repository.RouteRepository;
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
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final RouteMapper routeMapper;
    private final StationMapper stationMapper;
    private final DtoMapperService dtoMapperService; 

    @Override
    public RouteResponse createRoute(RouteRequest request) {
        log.info("Creating new route with name: {}", request.getName());
        if (routeRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalStateException("An active route with this name already exists.");
        }
        Route route = routeMapper.toEntity(request);
        Route savedRoute = routeRepository.save(route);
        return dtoMapperService.mapToRouteResponse(savedRoute);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getActiveRouteById(Long id) {
        Route route = routeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active route not found with id: " + id));
        return dtoMapperService.mapToRouteResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RouteResponse> listActiveRoutes(Pageable pageable) {
        return routeRepository.findAllByDeletedFalse(pageable)
                .map(dtoMapperService::mapToRouteResponse);
    }

    @Override
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        log.info("Updating route with id: {}", id);
        Route existingRoute = routeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active route not found with id: " + id));
        
        if (!existingRoute.getName().equalsIgnoreCase(request.getName()) && routeRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new IllegalStateException("An active route with this name already exists.");
        }

        routeMapper.updateFromDto(request, existingRoute);
        Route updatedRoute = routeRepository.save(existingRoute);
        return dtoMapperService.mapToRouteResponse(updatedRoute);
    }

    @Override
    public void softDeleteRoute(Long id) {
        log.info("Attempting to SOFT DELETE route with id: {}", id);
        Route route = routeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active route not found with id: " + id));
        
        route.setDeleted(true);
        routeRepository.save(route);
        log.info("Successfully soft-deleted route with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> listInactiveRoutes() {
        return routeRepository.findAllByDeletedTrue().stream()
                .map(dtoMapperService::mapToRouteResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RouteResponse restoreRoute(Long id) {
        log.info("Restoring route with id: {}", id);
        Route inactiveRoute = routeRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inactive route not found with id: " + id));
        
        inactiveRoute.setDeleted(false);
        inactiveRoute.setDeletedAt(null);
        Route restoredRoute = routeRepository.save(inactiveRoute);
        return dtoMapperService.mapToRouteResponse(restoredRoute);
    }

    @Override
    public void hardDeleteRoute(Long id) {
        log.warn("Attempting to HARD DELETE route with id: {}", id);
        if (!routeRepository.existsByIdAndDeletedFalse(id)) {
            throw new ResourceNotFoundException("Cannot hard delete. Active route not found with id: " + id);
        }
        routeRepository.hardDeleteById(id);
        log.warn("Successfully hard-deleted route with id: {}", id);
    }

    @Override
    public RouteResponse assignStationsToRoute(Long routeId, RouteStationRequest request) {
        log.info("Assigning {} stations to route with id: {}", request.getStationIds().size(), routeId);
        Route route = routeRepository.findByIdAndDeletedFalse(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Active route not found with id: " + routeId));

        List<Station> stations = stationRepository.findAllByIdInAndDeletedFalse(request.getStationIds());
        if (stations.size() != request.getStationIds().size()) {
            throw new ResourceNotFoundException("One or more of the provided station IDs are invalid or refer to deleted stations.");
        }

        route.setStations(stations);
        Route updatedRoute = routeRepository.save(route);
        return dtoMapperService.mapToRouteResponse(updatedRoute);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PublicRouteResponse> listAllActiveRoutesForPublic(Pageable pageable) {
        return routeRepository.findAllByDeletedFalse(pageable)
                .map(routeMapper::toPublicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicStationResponse> getStationsForPublicRoute(Long routeId) {
        Route route = routeRepository.findByIdAndDeletedFalse(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Active route not found with id: " + routeId));
        
        return route.getStations().stream()
                .map(stationMapper::toPublicResponse)
                .collect(Collectors.toList());
    }
}