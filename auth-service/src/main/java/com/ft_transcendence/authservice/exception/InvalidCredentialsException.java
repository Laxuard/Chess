package com.ft_transcendence.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "invalid-credentials",
                "Email or password is incorrect");
    }
}
