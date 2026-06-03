package com.ft_transcendence.gateway.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import reactor.core.publisher.Mono;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.web.server.ResponseStatusException;
import com.ft_transcendence.gateway.domain.service.JwtService;
import org.springframework.http.server.reactive.ServerHttpRequest;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.List;

@Slf4j
@Component
public class SessionToJwtGatewayFilterFactory
        extends AbstractGatewayFilterFactory<SessionToJwtGatewayFilterFactory.Config> {

    private final JwtService jwtService;

    public SessionToJwtGatewayFilterFactory(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String traceId = ReactiveTraceContext.getTraceId(exchange);

            // 1. Safe Cookie Evaluation: Only evaluates requests routed to protected pages
            List<HttpCookie> rawSessionCookies = request.getCookies().get("SESSION");
            if (rawSessionCookies == null || rawSessionCookies.isEmpty()) {
                log.warn("Missing session cookie context container on guarded route request.");
                return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "An active cookie session is required to traverse this gateway proxy"));
            }

            return exchange.getSession().flatMap(webSession -> {
                Object userIdAttr = webSession.getAttribute("userId");
                Object rolesAttr = webSession.getAttribute("roles");

                if (userIdAttr == null || rolesAttr == null) {
                    log.warn("Access intercept - Missing or expired active Redis context.");
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "An active cookie session is required to traverse this gateway proxy"));
                }

                String userId = userIdAttr.toString();
                String sessionId = webSession.getId();
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesAttr;

                String transitJwt = jwtService.mint(userId, roles, sessionId, traceId);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .headers(headers -> {
                            headers.remove(HttpHeaders.COOKIE);
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + transitJwt);
                        })
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            });
        };
    }

    public static class Config {
    }
}