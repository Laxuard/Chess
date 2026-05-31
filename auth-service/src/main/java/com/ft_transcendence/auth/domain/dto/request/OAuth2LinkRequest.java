package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import java.util.UUID;

@Builder
public record OAuth2LinkRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider,

        @NotBlank(message = "Provider unique identifier (sub ID) cannot be blank")
        String providerId
) {}
