package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.ft_transcendence.auth.domain.model.AuthProvider;

public record OAuth2SyncRequest(
        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider,

        @NotBlank(message = "Provider unique identifier (sub ID) cannot be blank")
        String providerId,

        @NotBlank(message = "Email address cannot be blank")
        @Email(message = "Invalid email address format supplied")
        String email,

        @NotBlank(message = "User full name cannot be blank")
        String name,

        @NotBlank(message = "Avatar URL cannot be blank")
        String avatarUrl
) {}