package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {
    String getRegistrationId();
    String getName(OAuth2User oAuth2User);
    String getEmail(OAuth2User oAuth2User);
    String getAvatarUrl(OAuth2User oAuth2User);
    String getProviderId(OAuth2User oAuth2User);
}