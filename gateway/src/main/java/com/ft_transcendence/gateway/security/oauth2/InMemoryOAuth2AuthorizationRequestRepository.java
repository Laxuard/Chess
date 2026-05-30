package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOAuth2AuthorizationRequestRepository 
        implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // Store the temporary in-flight handshake states entirely in Gateway RAM
    private final ConcurrentHashMap<String, OAuth2AuthorizationRequest> ramCache = new ConcurrentHashMap<>();

    @Override
    public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        if (state == null) {
            return Mono.empty();
        }
        return Mono.justOrEmpty(ramCache.get(state));
    }

    @Override
    public Mono<Void> saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
        if (authorizationRequest != null && authorizationRequest.getState() != null) {
            ramCache.put(authorizationRequest.getState(), authorizationRequest);
        }
        return Mono.empty();
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        if (state == null) {
            return Mono.empty();
        }
        // Evict from RAM instantly the moment the user completes the loop
        return Mono.justOrEmpty(ramCache.remove(state));
    }
}
