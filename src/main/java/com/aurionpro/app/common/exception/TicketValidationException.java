package com.aurionpro.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) 
public class TicketValidationException extends RuntimeException {
    public TicketValidationException(String message) {
        super(message);
    }
}