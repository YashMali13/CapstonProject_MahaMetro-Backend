package com.aurionpro.app.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class ApiErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
    private String path;
    private ErrorDetails error;

    public ApiErrorResponse(String path, String code, String message) {
        this.timestamp = Instant.now();
        this.path = path;
        this.error = ErrorDetails.builder()
                .code(code)
                .message(message)
                .build();
    }
    
    public ApiErrorResponse(String path, String code, String message, Map<String, Object> details) {
        this.timestamp = Instant.now();
        this.path = path;
        this.error = ErrorDetails.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}