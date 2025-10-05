package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.PublicRouteResponse;
import com.aurionpro.app.dto.RouteRequest;
import com.aurionpro.app.dto.RouteResponse;
import com.aurionpro.app.dto.RouteSummaryResponse;
import com.aurionpro.app.entity.Route;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "stations", ignore = true)
    RouteResponse toResponse(Route route);
    
    Route toEntity(RouteRequest request);
    PublicRouteResponse toPublicResponse(Route route);
    RouteSummaryResponse toSummaryResponse(Route route);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(RouteRequest request, @MappingTarget Route route);
}