package com.ft_transcendence.gateway.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ResponseStatusException;
import com.ft_transcendence.gateway.domain.service.JwtService;
import org.springframework.http.server.reactive.ServerHttpRequest;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import java.util.Set;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionToJwtGatewayFilterFactory implements AbstractGatewayFilterFactory<SessionToJwtGatewayFilterFactory.Config> {

    private final JwtService jwtService;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String traceId = ReactiveTraceContext.getTraceId(exchange);

        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange.mutate()
                    .request(r -> r.header(ReactiveTraceContext.TRACE_HEADER, traceId))
                    .build());
        }

        List<HttpCookie> rawSessionCookies = request.getCookies().get("SESSION");
        if (rawSessionCookies == null || rawSessionCookies.isEmpty()) {
            log.error("Missing critical 'SESSION' cookie container on incoming proxy route request.");
        }

        return exchange.getSession().flatMap(webSession -> {
            log.debug("WebFlux Active Session Resolved ID: {}", webSession.getId());

            Object userIdAttr = webSession.getAttribute("userId");
            Object rolesAttr = webSession.getAttribute("roles");

            if (userIdAttr == null || rolesAttr == null) {
                log.warn("Unauthorized access intercept - Missing or expired active Redis context.");

                return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "An active cookie session is required to traverse this gateway proxy"
                ));
            }

            String userId = userIdAttr.toString();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesAttr;

            String transitJwt = jwtService.mint(userId, roles, traceId);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove(HttpHeaders.COOKIE);
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + transitJwt);

                        headers.remove(ReactiveTraceContext.TRACE_HEADER);
                        headers.set(ReactiveTraceContext.TRACE_HEADER, traceId);
                    })
                    .build();

            log.info("Session validated successfully -> Attaching internal transit JWT for subject user: {}", userId);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}