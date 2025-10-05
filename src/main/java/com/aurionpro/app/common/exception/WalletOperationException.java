package com.aurionpro.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) 
public class WalletOperationException extends RuntimeException {
    public WalletOperationException(String message) {
        super(message);
    }
}