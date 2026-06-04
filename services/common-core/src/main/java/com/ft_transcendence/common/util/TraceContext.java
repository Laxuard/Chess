package com.ft_transcendence.common.util;

import org.slf4j.MDC;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

public final class TraceContext {

    public static final String TRACE_KEY = "trace_id";
    public static final String TRACE_HEADER = "X-Trace-Id";

    private TraceContext() {} // Suppress instantiation

    /**
     * Resolves the active trace ID across all compilation targets uniformly.
     * Checks MDC first, falls back to request attributes, and isolates a safe fallback if entirely decoupled.
     */
    public static String getTraceId(HttpServletRequest request) {
        // 1. Primary Source: Extract from the SLF4J Thread-Local Logging Context
        String traceId = MDC.get(TRACE_KEY);

        // 2. Secondary Fallback: Extract from the HttpServletRequest attribute space
        if ((traceId == null || traceId.isBlank()) && request != null) {
            traceId = (String) request.getAttribute(TRACE_KEY);
        }

        // 3. Tertiary Fallback: Read straight from incoming client headers if attributes were cleared
        if ((traceId == null || traceId.isBlank()) && request != null) {
            traceId = request.getHeader(TRACE_HEADER);
        }

        // 4. Emergency Fallback: Generate a fresh one if called outside an active HTTP servlet lifecycle thread
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        return traceId;
    }
}
