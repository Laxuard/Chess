package com.ft_transcendence.gateway.security.oauth2;

import lombok.Builder;

@Builder
public record OAuth2LinkRequest(
        String provider,
        String providerId
) {}
