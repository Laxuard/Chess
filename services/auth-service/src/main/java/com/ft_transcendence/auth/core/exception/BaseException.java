package com.ft_transcendence.auth.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends com.ft_transcendence.common.exception.BaseException {

    protected BaseException(HttpStatus httpStatus, String errorCode, String message) {
        super(httpStatus, errorCode, message);
    }

}
