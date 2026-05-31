package com.ft_transcendence.gateway.security.oauth2;

import com.ft_transcendence.gateway.security.oauth2.OAuth2UserInfoCompositeExtractor.OAuth2SyncPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private final WebClient.Builder webClientBuilder;
    private final OAuth2UserInfoCompositeExtractor extractorFactory;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Override
    @NullMarked
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        // 1. Delegate the data parsing to our decoupled strategy layer
        OAuth2SyncPayload syncBody = extractorFactory.extract(registrationId, oauthToken.getPrincipal());

        return webFilterExchange.getExchange().getSession().flatMap(webSession -> {
            String existingUserId = webSession.getAttribute("userId");
            Boolean isLinkingInProgress = webSession.getAttribute("oauth2_linking_in_progress");

            // Clear the linking handshake flag to prevent stale reuse
            webSession.getAttributes().remove("oauth2_linking_in_progress");

            if (existingUserId != null && Boolean.TRUE.equals(isLinkingInProgress)) {
                // ── CASE A: THE USER IS LINKING AN ACCOUNT ───────────────────
                log.info("Active user [{}] is linking external identity provider [{}]...", existingUserId, syncBody.provider());

                OAuth2LinkRequest linkRequest = OAuth2LinkRequest.builder()
                        .userId(java.util.UUID.fromString(existingUserId))
                        .provider(syncBody.provider())
                        .providerId(syncBody.providerId())
                        .build();

                return webClientBuilder.build()
                        .post()
                        .uri("https://auth-service/oauth2/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(linkRequest)
                        .retrieve()
                        .toBodilessEntity()
                        .then(redirectStrategy.sendRedirect(
                                webFilterExchange.getExchange(),
                                URI.create("http://localhost:5173/dashboard?link=success")
                        ))
                        .onErrorResume(ex -> {
                            log.error("Failed to link social identity record", ex);
                            return redirectStrategy.sendRedirect(
                                    webFilterExchange.getExchange(),
                                    URI.create("http://localhost:5173/dashboard?link=error")
                            );
                        });
            }

            // ── CASE B: STANDARD SSO LOGIN FLOW ──────────────────────────
            log.info("OAuth2 login completed via [{}]. Executing downstream identity sync...", syncBody.provider());

            return webClientBuilder.build()
                    .post()
                    .uri("https://auth-service/oauth2/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(syncBody)
                    .retrieve()
                    .bodyToMono(UserSummaryResponse.class)
                    .flatMap(userSummary -> {
                        // 3. Populate unified Redis Session Attributes
                        webSession.getAttributes().put("userId", userSummary.userId());
                        webSession.getAttributes().put("roles", userSummary.roles());
                        webSession.getAttributes().put("isFullyAuthenticated", true);

                        log.info("OAuth Session registration completed for User ID [{}]", userSummary.userId());

                        // 4. Force frontend client redirection to React Dev Server Dashboard
                        return redirectStrategy.sendRedirect(
                                webFilterExchange.getExchange(),
                                URI.create("http://localhost:5173/dashboard")
                        );
                    })
                    .onErrorResume(ex -> {
                        log.error("OAuth2 SSO login synchronization failed", ex);

                        String errorType = "auth_error";
                        if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException.Conflict) {
                            errorType = "email_taken";
                        }

                        return redirectStrategy.sendRedirect(
                                webFilterExchange.getExchange(),
                                URI.create("http://localhost:5173/login?error=" + errorType)
                        );
                    });
        });
    }

    private record UserSummaryResponse(String userId, List<String> roles) {}
}