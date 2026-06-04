package com.ft_transcendence.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
