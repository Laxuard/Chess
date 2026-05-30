package com.ft_transcendence.auth.domain.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String status,                      // "AUTHENTICATED" or "AWAITING_MFA"
        UserSummary user,                   // Populated ONLY when status is "AUTHENTICATED"
        MfaDetails mfaDetails               // Populated ONLY when status is "AWAITING_MFA"
) {

    @Builder
    public record UserSummary(
            UUID userId,
            String username,
            String email,
            List<String> roles
    ) {}

    @Builder
    public record MfaDetails(
            List<TwoFactorMethodType> availableMethods
    ) {}
}