package com.ft_transcendence.auth.domain.dto.request;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.ft_transcendence.auth.domain.model.AuthProvider;

@Builder
public record OAuth2LinkRequest(
        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider,

        @NotBlank(message = "Provider unique identifier (sub ID) cannot be blank")
        String providerId
) {}
