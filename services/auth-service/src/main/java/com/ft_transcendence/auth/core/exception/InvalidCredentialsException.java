package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {

    /**
     * Default constructor used by standard password login failures.
     */
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Email or password is incorrect");
    }

    /**
     * Overloaded constructor allowing custom contextual messages
     * (like your specific MFA challenge failures).
     */
    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "invalid-credentials", message);
    }
}