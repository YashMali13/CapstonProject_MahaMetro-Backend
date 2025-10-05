package com.aurionpro.app.controller;

import com.aurionpro.app.dto.RouteRequest;
import com.aurionpro.app.dto.RouteResponse;
import com.aurionpro.app.dto.RouteStationRequest;
import com.aurionpro.app.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        return new ResponseEntity<>(routeService.createRoute(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<RouteResponse>> listActiveRoutes(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(routeService.listActiveRoutes(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RouteResponse> getActiveRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getActiveRouteById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> softDeleteRoute(@PathVariable Long id) {
        routeService.softDeleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<RouteResponse>> listInactiveRoutes() {
        return ResponseEntity.ok(routeService.listInactiveRoutes());
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> restoreRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.restoreRoute(id));
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> hardDeleteRoute(@PathVariable Long id) {
        routeService.hardDeleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{routeId}/stations")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RouteResponse> assignStationsToRoute(@PathVariable Long routeId, @Valid @RequestBody RouteStationRequest request) {
        return ResponseEntity.ok(routeService.assignStationsToRoute(routeId, request));
    }
}