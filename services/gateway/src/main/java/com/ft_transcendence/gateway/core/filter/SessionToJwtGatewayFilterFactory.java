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

                // Session Hijacking Protection: Validate Fingerprint
                String storedFingerprint = webSession.getAttribute("sessionFingerprint");
                if (storedFingerprint != null) {
                    String currentFingerprint = generateSessionFingerprint(exchange);
                    if (!storedFingerprint.equals(currentFingerprint)) {
                        log.error("CRITICAL: Session fingerprint mismatch detected! Possible session hijacking attempt! " +
                                "Invalidating session [{}]. Stored: {}, Current: {}", 
                                webSession.getId(), storedFingerprint, currentFingerprint);
                        
                        return webSession.invalidate().then(
                            Mono.error(new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Session compromised. Please re-authenticate."))
                        );
                    }
                } else {
                    String currentFingerprint = generateSessionFingerprint(exchange);
                    webSession.getAttributes().put("sessionFingerprint", currentFingerprint);
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

    private String generateSessionFingerprint(org.springframework.web.server.ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        String ip;
        if (xff != null && !xff.isBlank()) {
            ip = xff.split(",")[0].trim();
        } else {
            java.net.InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            ip = (remoteAddress != null && remoteAddress.getAddress() != null) 
                    ? remoteAddress.getAddress().getHostAddress() 
                    : "unknown";
        }
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String rawString = ip + "|" + userAgent;
            byte[] hash = digest.digest(rawString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return ip + "|" + userAgent;
        }
    }

    public static class Config {
    }
}