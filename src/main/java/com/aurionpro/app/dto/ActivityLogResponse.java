package com.aurionpro.app.dto;

import com.aurionpro.app.entity.ActionType;
import com.aurionpro.app.entity.ActivityLog;
import lombok.Data;
import java.time.Instant;

@Data
public class ActivityLogResponse {
    private Long id;
    private Instant createdAt;
    private Long userId;
    private ActionType event;
    private String details;
    private String entityType;
    private Long entityId;
    private String referenceId;
    private ActivityLog.LogLevel level;
}