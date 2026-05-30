package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "invalid-credentials",
                "Email or password is incorrect");
    }
}
