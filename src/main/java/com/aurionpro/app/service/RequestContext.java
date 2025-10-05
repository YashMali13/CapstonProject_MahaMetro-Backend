package com.aurionpro.app.service;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Data
public class RequestContext {
    private String ipAddress;
    private String userAgent;
}