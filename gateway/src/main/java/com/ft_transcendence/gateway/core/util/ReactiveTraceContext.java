package com.ft_transcendence.gateway.core.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import java.util.UUID;

public final class ReactiveTraceContext {

    public static final String TRACE_KEY = "trace_id";
    public static final String TRACE_HEADER = "X-Trace-Id";

    private ReactiveTraceContext() {}

    public static String getTraceId(ServerWebExchange exchange) {
        if (exchange == null) return UUID.randomUUID().toString();

        // 1. Try to read from exchange attributes first
        String traceId = exchange.getAttribute(TRACE_KEY);

        // 2. Fallback: Parse from the raw incoming HTTP headers
        if (traceId == null || traceId.isBlank()) {
            ServerHttpRequest request = exchange.getRequest();
            traceId = request.getHeaders().getFirst(TRACE_HEADER);
        }

        // 3. Emergency: Fallback to a fresh generation if missing
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        return traceId;
    }
}