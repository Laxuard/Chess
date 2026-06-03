package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import com.ft_transcendence.auth.domain.model.UserAuth;

import java.util.List;
import java.util.UUID;

@Builder
public record OAuth2UserSummary(
        UUID userId,
        List<String> roles
) {
    public static OAuth2UserSummary fromEntity(UserAuth user) {
        return OAuth2UserSummary.builder()
                .userId(user.getUserId())
                .roles(user.getRoles().stream().map(Enum::name).toList())
                .build();
    }

}