package com.aurionpro.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class FareRuleNotFoundException extends RuntimeException {
    public FareRuleNotFoundException(String message) {
        super(message);
    }
}