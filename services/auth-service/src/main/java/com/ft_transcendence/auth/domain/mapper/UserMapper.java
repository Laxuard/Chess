package com.ft_transcendence.auth.domain.mapper;

import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import org.springframework.stereotype.Component;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;
import com.ft_transcendence.auth.domain.dto.response.UserProfileResponse;

import java.util.List;

@Component
public class UserMapper {

    public AuthResponse toRegisterResponse(UserAuth user) {
        return AuthResponse.builder()
                .status("AUTHENTICATED")
                .user(mapToSummary(user))
                .build();
    }

    public AuthResponse toLoginResponse(AuthStateResult result) {
        if ("AWAITING_MFA".equals(result.status())) {
            return AuthResponse.builder()
                    .status("AWAITING_MFA")
                    .mfaDetails(AuthResponse.MfaDetails.builder()
                            .availableMethods(result.availableMethods())
                            .build())
                    .build();
        }

        return AuthResponse.builder()
                .status("AUTHENTICATED")
                .user(mapToSummary(result.user()))
                .build();
    }

    public UserProfileResponse toProfileResponse(UserAuth user) {
        List<String> userRoles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        List<UserProfileResponse.MfaMethodSummary> mfaSummaries = user.getTwoFactorMethods().stream()
                .map(m -> UserProfileResponse.MfaMethodSummary.builder()
                        .methodType(m.getMethodType())
                        .isVerified(m.isVerified())
                        .lastUsedAt(m.getLastUsedAt())
                        .build())
                .toList();

        List<UserProfileResponse.IdentitySummary> identitySummaries = user.getIdentities().stream()
                .map(i -> UserProfileResponse.IdentitySummary.builder()
                        .provider(i.getProvider())
                        .providerId(i.getProviderId())
                        .lastLoginAt(i.getLastLoginAt())
                        .build())
                .toList();

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .roles(userRoles)
                .enabled(user.isEnabled())
                .is2faEnabled(user.is2faEnabled())
                .hasPassword(hasPassword)
                .twoFactorMethods(mfaSummaries)
                .identities(identitySummaries)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AuthResponse.UserSummary mapToSummary(UserAuth user) {
        List<String> userRoles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return AuthResponse.UserSummary.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(userRoles)
                .build();
    }
}