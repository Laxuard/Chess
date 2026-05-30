package com.ft_transcendence.auth.domain.mapper;

import org.springframework.stereotype.Component;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;

@Component
public class UserMapper {

    public AuthResponse toRegisterResponse(UserAuth userAuth) {
        if (userAuth == null) return null;

        return new AuthResponse(
                userAuth.getUserId(),
                userAuth.getUsername(),
                userAuth.is2faEnabled()
        );
    }
}