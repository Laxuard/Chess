package com.ft_transcendence.common.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import com.ft_transcendence.common.util.TraceContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.net.URI;
import java.time.Instant;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Value("${app.error-docs-url:https://api.transcendence.com/errors/}")
    private String docBaseUrl;

    @Value("${spring.application.name}")
    private String serviceId;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        String errorCode = "unauthorized";

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Full authentication is required to access this resource"
        );

        problem.setType(URI.create(docBaseUrl + errorCode));
        problem.setTitle("Unauthorized Access");
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("service_origin", serviceId);
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("trace_id", TraceContext.getTraceId(request));

        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
