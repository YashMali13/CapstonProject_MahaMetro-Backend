package com.aurionpro.app.service;

import com.aurionpro.app.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RouteService {
    RouteResponse createRoute(RouteRequest request);
    RouteResponse getActiveRouteById(Long id);
    Page<RouteResponse> listActiveRoutes(Pageable pageable);
    RouteResponse updateRoute(Long id, RouteRequest request);
    void softDeleteRoute(Long id);
    List<RouteResponse> listInactiveRoutes();
    RouteResponse restoreRoute(Long id);
    void hardDeleteRoute(Long id);
    RouteResponse assignStationsToRoute(Long routeId, RouteStationRequest request);
    Page<PublicRouteResponse> listAllActiveRoutesForPublic(Pageable pageable);
    List<PublicStationResponse> getStationsForPublicRoute(Long routeId);
}