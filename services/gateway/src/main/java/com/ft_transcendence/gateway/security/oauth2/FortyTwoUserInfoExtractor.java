package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Objects;

@Component
public class FortyTwoUserInfoExtractor implements OAuth2UserInfoExtractor {
    @Override
    public String getRegistrationId() {
        return "fortytwo";
    }

    @Override
    public String getProviderId(OAuth2User oAuth2User) {
        Object id = oAuth2User.getAttribute("id");
        return Objects.requireNonNull(id, "42 API did not return an 'id' field").toString();
    }

    @Override
    public String getEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    @Override
    public String getName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("login");
    }

    @Override
    public String getAvatarUrl(OAuth2User oAuth2User) {
        Object imageObj = oAuth2User.getAttribute("image");
        if (imageObj instanceof Map<?, ?> imageMap) {
            Object link = imageMap.get("link");
            if (link instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return "/assets/avatars/default-placeholder.png";
    }
}