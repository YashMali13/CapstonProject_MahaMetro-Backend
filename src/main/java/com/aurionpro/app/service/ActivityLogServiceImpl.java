package com.aurionpro.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.entity.ActionType;
import com.aurionpro.app.entity.ActivityLog;
import com.aurionpro.app.repository.ActivityLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final RequestContext requestContext; 

    @Override
    public void log(ActionType event, String actorEmail, ActivityLog.LogLevel level, String details, String referenceId) {
        ActivityLog logEntry = ActivityLog.builder()
                .event(event)
                .actorEmail(actorEmail)
                .ipAddress(requestContext.getIpAddress()) 
                .userAgent(requestContext.getUserAgent()) 
                .level(level)
                .details(details)
                .referenceId(referenceId)
                .build();
        activityLogRepository.save(logEntry);
    }
}