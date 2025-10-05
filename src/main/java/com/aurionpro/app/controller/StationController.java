package com.aurionpro.app.controller;

import com.aurionpro.app.dto.StationDetailResponse;
import com.aurionpro.app.dto.StationRequest;
import com.aurionpro.app.dto.StationResponse;
import com.aurionpro.app.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StationResponse> createStation(@Valid @RequestBody StationRequest request) {
        return new ResponseEntity<>(stationService.createStation(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<StationResponse>> listActiveStations(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(stationService.listActiveStations(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StationResponse> getActiveStationById(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getActiveStationById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StationResponse> updateStation(@PathVariable Long id, @Valid @RequestBody StationRequest request) {
        return ResponseEntity.ok(stationService.updateStation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> softDeleteStation(@PathVariable Long id) {
        stationService.softDeleteStation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<StationResponse>> listInactiveStations() {
        return ResponseEntity.ok(stationService.listInactiveStations());
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StationResponse> restoreStation(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.restoreStation(id));
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> hardDeleteStation(@PathVariable Long id) {
        stationService.hardDeleteStation(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/with-routes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<StationDetailResponse>> listActiveStationsWithRoutes() {
        return ResponseEntity.ok(stationService.listActiveStationsWithRoutes());
    }
}