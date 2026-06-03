package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.AuthProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
        UUID userId,
        String username,
        String email,
        List<String> roles,
        boolean enabled,
        boolean is2faEnabled,
        boolean hasPassword,
        List<MfaMethodSummary> twoFactorMethods,
        List<IdentitySummary> identities,
        LocalDateTime createdAt
) {

    @Builder
    public record MfaMethodSummary(
            TwoFactorMethodType methodType,
            boolean isVerified,
            LocalDateTime lastUsedAt
    ) {}

    @Builder
    public record IdentitySummary(
            AuthProvider provider,
            String providerId,
            LocalDateTime lastLoginAt
    ) {}
}
