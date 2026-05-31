package com.ft_transcendence.gateway.security.oauth2;

import com.ft_transcendence.gateway.security.oauth2.OAuth2UserInfoCompositeExtractor.OAuth2SyncPayload;
import com.ft_transcendence.gateway.domain.service.JwtService;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;
    private final OAuth2UserInfoCompositeExtractor extractorFactory;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Override
    @NullMarked
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        ServerWebExchange exchange = webFilterExchange.getExchange();

        OAuth2SyncPayload syncPayload = extractorFactory.extract(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getPrincipal()
        );

        return exchange.getSession().flatMap(session -> {
            String existingUserId = session.getAttribute("userId");
            Boolean isLinkingInProgress = session.getAttribute("oauth2_linking_in_progress");

            // Evict handshake flag immediately to prevent stale reuse
            session.getAttributes().remove("oauth2_linking_in_progress");

            if (existingUserId != null && Boolean.TRUE.equals(isLinkingInProgress)) {
                return executeAccountLink(exchange, session, syncPayload, existingUserId);
            }

            return executeIdentitySync(exchange, session, syncPayload);
        });
    }

    // ── PRIVATE ORCHESTRATION EXTRACTIONS ───────────────────────────────────

    private Mono<Void> executeAccountLink(ServerWebExchange exchange, WebSession session,
                                          OAuth2SyncPayload payload, String userId) {
        log.info("Active user [{}] is linking external identity provider [{}]...", userId, payload.provider());

        String transitJwt = mintTransitToken(exchange, session, userId);
        OAuth2LinkRequest linkRequest = new OAuth2LinkRequest(payload.provider(), payload.providerId());

        return webClientBuilder.build()
                .post()
                .uri("https://auth-service/oauth2/link")
                .header("Authorization", "Bearer " + transitJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(linkRequest)
                .retrieve()
                .toBodilessEntity()
                .then(redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/dashboard?link=success")))
                .onErrorResume(ex -> {
                    log.error("Failed to link social identity record", ex);
                    return redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/dashboard?link=error"));
                });
    }

    private Mono<Void> executeIdentitySync(ServerWebExchange exchange, WebSession session, OAuth2SyncPayload payload) {
        log.info("OAuth2 login completed via [{}]. Executing downstream identity sync...", payload.provider());

        return webClientBuilder.build()
                .post()
                .uri("https://auth-service/oauth2/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(UserSummaryResponse.class)
                .flatMap(userSummary -> {
                    session.getAttributes().put("userId", userSummary.userId());
                    session.getAttributes().put("roles", userSummary.roles());
                    session.getAttributes().put("isFullyAuthenticated", true);

                    log.info("OAuth Session registration completed for User ID [{}]", userSummary.userId());
                    return redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/dashboard"));
                })
                .onErrorResume(ex -> {
                    log.error("OAuth2 SSO login synchronization failed", ex);
                    String errorParam = resolveErrorParam(ex);
                    return redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/login?error=" + errorParam));
                });
    }

    // ── PRIVATE UTILITY SCOPES ──────────────────────────────────────────────

    private String mintTransitToken(ServerWebExchange exchange, WebSession session, String userId) {
        String traceId = ReactiveTraceContext.getTraceId(exchange);

        List<String> roles = session.getAttribute("roles");
        if (roles == null) {
            roles = List.of("ROLE_USER");
        }

        return jwtService.mint(userId, roles, session.getId(), traceId);
    }

    private String resolveErrorParam(Throwable ex) {
        if (ex instanceof WebClientResponseException.Conflict) {
            return "email_taken";
        }
        return "auth_error";
    }

    private record UserSummaryResponse(String userId, List<String> roles) {}
}