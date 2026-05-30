package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class MfaException extends BaseException {

    /**
     * Constructs a custom multi-factor authentication lifecycle exception.
     * Maps automatically to an HTTP 400 Bad Request and provides a clean error code.
     */
    public MfaException(String message) {
        super(HttpStatus.BAD_REQUEST, "mfa-validation-failed", message);
    }
}