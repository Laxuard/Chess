package com.ft_transcendence.gateway.core.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReactiveTraceIdFilter implements WebFilter {

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String existingHeader = exchange.getRequest().getHeaders().getFirst(ReactiveTraceContext.TRACE_HEADER);
        String traceId = (existingHeader != null && !existingHeader.isBlank()) ? existingHeader : UUID.randomUUID().toString();

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(ReactiveTraceContext.TRACE_HEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        mutatedExchange.getAttributes().put(ReactiveTraceContext.TRACE_KEY, traceId);
        mutatedExchange.getResponse().getHeaders().set(ReactiveTraceContext.TRACE_HEADER, traceId);

        // Pass the mutated exchange down the pipeline
        return chain.filter(mutatedExchange)
                .contextWrite(context -> context.put(ReactiveTraceContext.TRACE_KEY, traceId));
    }
}