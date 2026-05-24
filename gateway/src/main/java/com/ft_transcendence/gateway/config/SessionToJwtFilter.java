package com.ft_transcendence.gateway.config;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.core.io.buffer.DataBuffer;
import com.ft_transcendence.gateway.service.JwtService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionToJwtFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private static final String TRACE_HEADER = "X-Trace-Id";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String traceId = resolveTraceId(request);

        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(mutateTraceHeader(exchange, traceId));
        }

        // ─── DIAGNOSTIC DEBUG LOG 1: WHAT IS POSTMAN ACTUALLY SENDING? ───
        List<HttpCookie> rawSessionCookies = request.getCookies().get("SESSION");
        if (rawSessionCookies == null || rawSessionCookies.isEmpty()) {
            log.error("[DIAGNOSTIC - {}] ❌ CRITICAL: No cookie named 'SESSION' found in incoming HTTP headers!", traceId);
        } else {
            rawSessionCookies.forEach(cookie ->
                    log.info("[DIAGNOSTIC - {}] ℹ️ Incoming Header Cookie Value: SESSION={}", traceId, cookie.getValue())
            );
        }

        return exchange.getSession().flatMap(webSession -> {
            // ─── DIAGNOSTIC DEBUG LOG 2: WHAT IS THE GATEWAY LOOKING FOR IN REDIS? ───
            log.info("[DIAGNOSTIC - {}] ℹ️ WebFlux Resolved Session ID: {}", traceId, webSession.getId());
            log.info("[DIAGNOSTIC - {}] ℹ️ WebFlux Session Active Key-Set Keys: {}", traceId, webSession.getAttributes().keySet());

            Object userIdAttr = webSession.getAttribute("userId");
            Object rolesAttr = webSession.getAttribute("roles");

            if (userIdAttr == null || rolesAttr == null) {
                log.warn("[{}] Unauthorized access attempt - Missing/Expired Session", traceId);
                return handleUnauthorized(exchange);
            }

            String userId = userIdAttr.toString();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesAttr;

            String transitJwt = jwtService.mint(userId, roles, traceId);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove(HttpHeaders.COOKIE);
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + transitJwt);
                        headers.set(TRACE_HEADER, traceId);
                    })
                    .build();

            log.info("[{}] Session validated successfully -> Attaching internal JWT for user: {}", traceId, userId);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
    }

    private String resolveTraceId(ServerHttpRequest request) {
        String existing = request.getHeaders().getFirst(TRACE_HEADER);
        return (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();
    }

    private ServerWebExchange mutateTraceHeader(ServerWebExchange exchange, String traceId) {
        return exchange.mutate()
                .request(r -> r.header(TRACE_HEADER, traceId))
                .build();
    }

    // Modern stateless RFC 9457 error builder to reject unauthenticated requests right at the Gateway edge
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        String body = """
                {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Active cookie session required"}""";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
