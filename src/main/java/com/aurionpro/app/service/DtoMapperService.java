package com.aurionpro.app.service;

import com.aurionpro.app.dto.RouteResponse;
import com.aurionpro.app.dto.StationDetailResponse;
import com.aurionpro.app.entity.Route;
import com.aurionpro.app.entity.Station;

public interface DtoMapperService {
    RouteResponse mapToRouteResponse(Route route);
    StationDetailResponse mapToStationDetailResponse(Station station);
}