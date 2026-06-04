package com.ft_transcendence.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BaseException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
