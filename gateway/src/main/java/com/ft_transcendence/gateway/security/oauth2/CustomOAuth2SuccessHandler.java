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

        log.info("OAuth2 login completed via [{}]. Executing downstream identity sync...", syncBody.provider());

        // 2. Dispatch the back-channel mTLS synchronization request
        return webClientBuilder.build()
                .post()
                .uri("https://auth-service/oauth2/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(syncBody)
                .retrieve()
                .bodyToMono(UserSummaryResponse.class)
                .flatMap(userSummary -> webFilterExchange.getExchange().getSession().flatMap(webSession -> {

                    // 3. Populate unified Redis Session Attributes
                    webSession.getAttributes().put("userId", userSummary.userId());
                    webSession.getAttributes().put("roles", userSummary.roles());
                    webSession.getAttributes().put("isFullyAuthenticated", true);

                    log.info("OAuth Session registration completed for User ID [{}]", userSummary.userId());

                    // 4. Force frontend client redirection
                    return redirectStrategy.sendRedirect(
                            webFilterExchange.getExchange(),
                            URI.create("https://localhost:8080/")
                    );
                }));
    }

    private record UserSummaryResponse(String userId, List<String> roles) {}
}