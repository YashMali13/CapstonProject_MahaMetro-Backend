package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.ActivityLogResponse;
import com.aurionpro.app.entity.ActivityLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {
    ActivityLogResponse toResponse(ActivityLog activityLog);
}