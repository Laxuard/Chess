package com.ft_transcendence.gateway.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisServerOAuth2AuthorizationRequestRepository 
        implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String REDIS_KEY_PREFIX = "oauth2_auth_request:";
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    @NullMarked
    public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        if (state == null) {
            return Mono.empty();
        }
        return reactiveRedisTemplate.opsForValue()
                .get(REDIS_KEY_PREFIX + state)
                .map(this::deserialize)
                .onErrorResume(ex -> {
                    log.error("Failed to load authorization request from Redis", ex);
                    return Mono.empty();
                });
    }

    @Override
    @NullMarked
    public Mono<Void> saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
        if (authorizationRequest == null || authorizationRequest.getState() == null) {
            return Mono.empty();
        }

        String state = authorizationRequest.getState();
        String serialized = serialize(authorizationRequest);
        if (serialized == null) {
            return Mono.empty();
        }

        // 1. Prepare our isolation string persistent task
        Mono<Boolean> saveStateMono = reactiveRedisTemplate.opsForValue()
                .set(REDIS_KEY_PREFIX + state, serialized, STATE_TTL);

        // 2. Safely mutate the attributes map and force a state persistence save flush
        boolean isLink = exchange.getRequest().getQueryParams().containsKey("link");
        Mono<Void> updateSessionMono = exchange.getSession().flatMap(session -> {
            if (isLink) {
                session.getAttributes().put("oauth2_linking_in_progress", true);
                log.debug("Marked active session [{}] as in-flight account linking state", session.getId());
            } else {
                session.getAttributes().remove("oauth2_linking_in_progress");
            }
            // CRITICAL FIX: Explicitly invoke the session saver downstream flush!
            return session.save();
        });

        // Chain them synchronously to confirm both state writes land before redirection
        return saveStateMono.then(updateSessionMono);
    }

    @Override
    @NullMarked
    public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        if (state == null) {
            return Mono.empty();
        }
        String key = REDIS_KEY_PREFIX + state;
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .flatMap(serialized -> reactiveRedisTemplate.delete(key)
                        .thenReturn(deserialize(serialized)))
                .onErrorResume(ex -> {
                    log.error("Failed to remove authorization request from Redis", ex);
                    return Mono.empty();
                });
    }

    // ── BASE64 OBJECT SERIALIZATION HELPERS ─────────────────────────────────

    private String serialize(OAuth2AuthorizationRequest request) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(request);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Serialization of OAuth2AuthorizationRequest failed", e);
            return null;
        }
    }

    private OAuth2AuthorizationRequest deserialize(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (OAuth2AuthorizationRequest) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Deserialization of OAuth2AuthorizationRequest failed", e);
            return null;
        }
    }
}
