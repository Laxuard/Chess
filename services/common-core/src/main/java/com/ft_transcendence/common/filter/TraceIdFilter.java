package com.ft_transcendence.common.filter;

import org.slf4j.MDC;
import jakarta.servlet.*;
import org.springframework.core.Ordered;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import com.ft_transcendence.common.util.TraceContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String traceId = request.getHeader(TraceContext.TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(TraceContext.TRACE_KEY, traceId);
        request.setAttribute(TraceContext.TRACE_KEY, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TraceContext.TRACE_KEY);
        }
    }
}
