package com.ft_transcendence.gateway.security.oauth2;

import lombok.Builder;
import java.util.UUID;

@Builder
public record OAuth2LinkRequest(
        UUID userId,
        String provider,
        String providerId
) {}
