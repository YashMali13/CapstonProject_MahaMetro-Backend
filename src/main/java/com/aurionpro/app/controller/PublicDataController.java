package com.aurionpro.app.controller;

import com.aurionpro.app.dto.PublicRouteResponse;
import com.aurionpro.app.dto.PublicStationResponse;
import com.aurionpro.app.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metro")
@RequiredArgsConstructor
public class PublicDataController {

    private final RouteService routeService;

    @GetMapping("/routes")
    public ResponseEntity<Page<PublicRouteResponse>> listAllActiveRoutes(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(routeService.listAllActiveRoutesForPublic(pageable));
    }

    @GetMapping("/routes/{routeId}/stations")
    public ResponseEntity<List<PublicStationResponse>> getStationsForRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeService.getStationsForPublicRoute(routeId));
    }
}