package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resource) {
        super(HttpStatus.NOT_FOUND, "resource-not-found",
                resource + " was not found");
    }
}
