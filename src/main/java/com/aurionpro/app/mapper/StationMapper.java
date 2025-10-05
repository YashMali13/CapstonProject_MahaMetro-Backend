package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.PublicStationResponse;
import com.aurionpro.app.dto.StationDetailResponse;
import com.aurionpro.app.dto.StationRequest;
import com.aurionpro.app.dto.StationResponse;
import com.aurionpro.app.entity.Station;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface StationMapper {
    
    @Mapping(target = "routes", ignore = true)
    StationDetailResponse toDetailResponse(Station station);

    Station toEntity(StationRequest request);
    
    StationResponse toResponse(Station station);
    
    PublicStationResponse toPublicResponse(Station station);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(StationRequest request, @MappingTarget Station station);
}