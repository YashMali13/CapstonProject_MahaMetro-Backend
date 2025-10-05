package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.ValidationHistoryResponse;
import com.aurionpro.app.entity.Validation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ValidationMapper {

    @Mapping(source = "id", target = "validationId")
    @Mapping(source = "createdAt", target = "scanTime")
    @Mapping(source = "station.name", target = "stationName")
    @Mapping(source = "staffMember.id", target = "staffMemberId")
    ValidationHistoryResponse toResponse(Validation validation);
}