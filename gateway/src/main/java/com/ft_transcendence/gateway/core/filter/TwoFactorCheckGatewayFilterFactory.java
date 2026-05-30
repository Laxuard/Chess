package com.ft_transcendence.gateway.core.filter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

@Slf4j
@Component
public class TwoFactorCheckGatewayFilterFactory extends AbstractGatewayFilterFactory<TwoFactorCheckGatewayFilterFactory.Config> {

    public TwoFactorCheckGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> exchange.getSession().flatMap(webSession -> {
            Object isFullyAuthenticatedAttr = webSession.getAttribute("isFullyAuthenticated");

            if (isFullyAuthenticatedAttr == null || !Boolean.parseBoolean(isFullyAuthenticatedAttr.toString())) {
                log.warn("Access Intercept - User session requires completed 2FA verification challenge.");
                return Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Full multi-factor authentication validation is required to access this resource"
                ));
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}