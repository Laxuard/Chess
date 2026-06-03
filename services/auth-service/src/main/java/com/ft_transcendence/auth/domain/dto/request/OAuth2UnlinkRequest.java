package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import com.ft_transcendence.auth.domain.model.AuthProvider;

@Builder
public record OAuth2UnlinkRequest(
        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider
) {}
