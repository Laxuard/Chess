package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String resource) {
        super(HttpStatus.CONFLICT, "duplicate-resource",
                resource + " already exists");
    }
}
