package com.ft_transcendence.authservice.dto;

import org.springframework.stereotype.Component;
import com.ft_transcendence.authservice.model.UserAuth;
import com.ft_transcendence.authservice.dto.response.RegisterResponse;

@Component
public class UserMapper {

    public RegisterResponse toRegisterResponse(UserAuth userAuth) {
        if (userAuth == null) return null;

        return new RegisterResponse(
                userAuth.getUserId(),
                userAuth.getUsername(),
                userAuth.getEmail(),
                userAuth.getRole().name(),
                userAuth.getCreatedAt(),
                "User registered successfully. Please log in."
        );
    }
}