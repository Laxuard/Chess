package com.ft_transcendence.authservice.dto;

import org.springframework.stereotype.Component;
import com.ft_transcendence.authservice.model.UserAuth;
import com.ft_transcendence.authservice.dto.response.AuthResponse;

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