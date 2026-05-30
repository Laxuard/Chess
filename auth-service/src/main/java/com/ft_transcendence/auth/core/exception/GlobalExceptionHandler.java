package com.ft_transcendence.auth.core.exception;

import org.slf4j.MDC;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletRequest;
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

    @Value("${app.error-docs-url}")
    private String docBaseUrl;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        // Safely extract the raw HttpServletRequest to get the URI string
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

        // Cast to Object to match the parent framework signature requirement
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(addMetadata(problem));
    }

    // ── Spring Security Authentication Exceptions ─────────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        // Default to a generic unauthorized message
        String detailMessage = "Authentication failed";
        String errorCode = "unauthorized";

        // You can check the specific subclass to give better error messages
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

    // ── All domain exceptions via single handler ──────────────────────────
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());

        problemDetail.setType(URI.create(docBaseUrl + ex.getErrorCode()));
        problemDetail.setTitle(toTitle(ex.getErrorCode()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(ex.getHttpStatus()).body(addMetadata(problemDetail));
    }

    // ── Catch-all — unexpected, always log with stack trace ───────────────
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

    // ── Metadata ──────────────────────────────────────────────────────────
    private ProblemDetail addMetadata(ProblemDetail problem) {
        String traceId = MDC.get("trace_id");
        if (traceId == null) traceId = UUID.randomUUID().toString();

        problem.setProperty("trace_id", traceId);
        problem.setProperty("timestamp", Instant.now().toString());
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
