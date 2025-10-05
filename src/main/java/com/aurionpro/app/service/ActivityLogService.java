package com.aurionpro.app.service;

import com.aurionpro.app.entity.ActionType;
import com.aurionpro.app.entity.ActivityLog;

public interface ActivityLogService {
    void log(ActionType event, String actorEmail, ActivityLog.LogLevel level, String details, String referenceId);
}