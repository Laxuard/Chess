package com.ft_transcendence.auth.domain.mapper;

import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import org.springframework.stereotype.Component;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;

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