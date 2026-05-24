package com.ft_transcendence.authservice.config;

import org.slf4j.MDC;
import jakarta.servlet.*;
import org.springframework.core.Ordered;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put("trace_id", traceId);
        response.setHeader(TRACE_HEADER, traceId); // echo back so client can log it

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // critical — Tomcat reuses threads, always clear
        }
    }
}