package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Objects;

@Component
public class FortyTwoUserInfoExtractor implements OAuth2UserInfoExtractor {
    @Override
    public String getRegistrationId() {
        return "fortytwo";
    }

    @Override
    public String getProviderId(OAuth2User oAuth2User) {
        return String.valueOf(Objects.requireNonNull(oAuth2User.getAttribute("id")));
    }

    @Override
    public String getEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    @Override
    public String getName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("login");
    }
}