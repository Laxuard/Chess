package com.ft_transcendence.gateway.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2UserInfoCompositeExtractor {

    private final List<OAuth2UserInfoExtractor> extractors;

    public OAuth2SyncPayload extract(String registrationId, OAuth2User oAuth2User) {
        return extractors.stream()
                .filter(extractor -> extractor.supports(registrationId))
                .findFirst()
                .map(extractor -> new OAuth2SyncPayload(
                        registrationId.toUpperCase(),
                        extractor.getProviderId(oAuth2User),
                        extractor.getEmail(oAuth2User),
                        extractor.getName(oAuth2User)
                ))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId));
    }

    public record OAuth2SyncPayload(String provider, String providerId, String email, String name) {}
}