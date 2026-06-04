package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

@Component
public class OAuth2UserInfoCompositeExtractor {

    private final Map<String, OAuth2UserInfoExtractor> extractors;

    public OAuth2UserInfoCompositeExtractor(List<OAuth2UserInfoExtractor> extractorList) {
        this.extractors = extractorList.stream().collect(Collectors.toMap(
                extractor -> extractor.getRegistrationId().toLowerCase(),
                Function.identity()
        ));
    }

    public OAuth2SyncPayload extract(String registrationId, OAuth2User oAuth2User) {
        OAuth2UserInfoExtractor extractor = extractors.get(registrationId.toLowerCase());
        if (extractor == null) {
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
        return new OAuth2SyncPayload(
                registrationId.toUpperCase(),
                extractor.getProviderId(oAuth2User),
                extractor.getEmail(oAuth2User),
                extractor.getName(oAuth2User),
                extractor.getAvatarUrl(oAuth2User)
        );
    }

    public record OAuth2SyncPayload(String provider, String providerId, String email, String name, String avatarUrl) {}
}