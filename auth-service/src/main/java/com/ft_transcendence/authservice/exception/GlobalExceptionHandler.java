package com.ft_transcendence.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.error-docs-url}")
    private String docBaseUrl;

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());

        problem.setType(URI.create(docBaseUrl + "duplicate-resource"));
        problem.setTitle("Resource Already Exists");
        return addCustomMetadata(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Input validation failed");

        problem.setType(URI.create(docBaseUrl + "validation-failed"));
        problem.setTitle("Constraint Violation");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        problem.setProperty("invalid_params", errors);
        return addCustomMetadata(problem);
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntime(RuntimeException ignoredEx) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred.");

        problem.setType(URI.create(docBaseUrl + "internal-server-error"));
        problem.setTitle("Internal Server Error");
        return addCustomMetadata(problem);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ignoredEx) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "A critical system error occurred.");

        return addCustomMetadata(problem);
    }

    private ProblemDetail addCustomMetadata(ProblemDetail problem) {

        // Placeholder for now.
        String traceId = UUID.randomUUID().toString();
        problem.setProperty("trace_id", traceId);


        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
