package com.aurionpro.app.service;

import com.aurionpro.app.dto.RouteResponse;
import com.aurionpro.app.dto.StationDetailResponse;
import com.aurionpro.app.entity.Route;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.mapper.RouteMapper;
import com.aurionpro.app.mapper.StationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DtoMapperServiceImpl implements DtoMapperService {

    private final RouteMapper routeMapper;
    private final StationMapper stationMapper;

    @Override
    public RouteResponse mapToRouteResponse(Route route) {
        RouteResponse response = routeMapper.toResponse(route);
        if (route.getStations() != null) {
            response.setStations(
                route.getStations().stream()
                    .map(stationMapper::toResponse)
                    .collect(Collectors.toList())
            );
        }
        return response;
    }

    @Override
    public StationDetailResponse mapToStationDetailResponse(Station station) {
        StationDetailResponse response = stationMapper.toDetailResponse(station);
        if (station.getRoutes() != null) {
            response.setRoutes(
                station.getRoutes().stream()
                    .map(routeMapper::toSummaryResponse)
                    .collect(Collectors.toList())
            );
        }
        return response;
    }
}