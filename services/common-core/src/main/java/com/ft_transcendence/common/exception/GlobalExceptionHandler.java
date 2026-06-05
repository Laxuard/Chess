package com.ft_transcendence.common.exception;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletRequest;
import com.ft_transcendence.common.util.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${app.error-docs-url:https://api.transcendence.com/errors/}")
    private String docBaseUrl;

    @Value("${spring.application.name}")
    private String serviceId;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        var servletRequest = ((ServletWebRequest) request).getRequest();

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Input validation failed");

        problem.setType(URI.create(docBaseUrl + "validation-failed"));
        problem.setTitle("Constraint Violation");
        problem.setInstance(URI.create(servletRequest.getRequestURI()));
        problem.setProperty("invalid_params", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(addMetadata(problem));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        String detailMessage = "Authentication failed";
        String errorCode = "unauthorized";

        if (ex instanceof BadCredentialsException) {
            detailMessage = "Email or password is incorrect";
            errorCode = "invalid-credentials";
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, detailMessage);

        problem.setType(URI.create(docBaseUrl + errorCode));
        problem.setTitle(toTitle(errorCode));
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(addMetadata(problem));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());

        problemDetail.setType(URI.create(docBaseUrl + ex.getErrorCode()));
        problemDetail.setTitle(toTitle(ex.getErrorCode()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(ex.getHttpStatus()).body(addMetadata(problemDetail));
    }

    @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class)
    public ResponseEntity<ProblemDetail> handleConcurrencyFailure(
            org.springframework.dao.ConcurrencyFailureException ex, HttpServletRequest request) {

        log.warn("Database concurrency failure intercepted on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was modified by another concurrent transaction. Please retry your request.");

        problem.setType(URI.create(docBaseUrl + "concurrency-conflict"));
        problem.setTitle("Concurrency Conflict");
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(addMetadata(problem));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation intercepted on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "A database integrity constraint was violated (e.g., unique key collision or invalid reference).");

        problem.setType(URI.create(docBaseUrl + "data-integrity-violation"));
        problem.setTitle("Data Integrity Violation");
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(addMetadata(problem));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {} {}",
                request.getMethod(), request.getRequestURI(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");

        problem.setType(URI.create(docBaseUrl + "internal-error"));
        problem.setTitle("Internal Server Error");
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(addMetadata(problem));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            Object body,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {

        if (body instanceof ProblemDetail problem) {
            addMetadata(problem);
        }

        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ProblemDetail addMetadata(ProblemDetail problem) {
        problem.setProperty("service_origin", serviceId);
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("trace_id", TraceContext.getTraceId(null));
        return problem;
    }

    private String toTitle(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) return "Error";
        return Arrays.stream(errorCode.split("-"))
                .filter(w -> !w.isBlank())
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}
