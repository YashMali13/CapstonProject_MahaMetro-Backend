package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.FareRuleRequest;
import com.aurionpro.app.dto.FareRuleResponse;
import com.aurionpro.app.entity.FareRule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    FareRule toEntity(FareRuleRequest request);

    FareRuleResponse toResponse(FareRule fareRule);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FareRuleRequest request, @MappingTarget FareRule fareRule);
}