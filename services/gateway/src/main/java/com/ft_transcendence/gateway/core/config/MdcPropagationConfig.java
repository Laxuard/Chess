package com.ft_transcendence.gateway.core.config;

import org.slf4j.MDC;
import reactor.core.publisher.Hooks;
import jakarta.annotation.PostConstruct;
import io.micrometer.context.ContextRegistry;
import org.springframework.context.annotation.Configuration;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;

@Configuration
public class MdcPropagationConfig {

    @PostConstruct
    public void initializeReactiveMdcPropagation() {
        // 1. Core Reactive Hook: Enables global, automated context copying across threads
        Hooks.enableAutomaticContextPropagation();

        // 2. Map the tracking key between Reactor Context and SLF4J MDC ThreadLocals
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                ReactiveTraceContext.TRACE_KEY,
                () -> MDC.get(ReactiveTraceContext.TRACE_KEY),
                traceId -> MDC.put(ReactiveTraceContext.TRACE_KEY, traceId),
                () -> MDC.remove(ReactiveTraceContext.TRACE_KEY)
        );
    }
}