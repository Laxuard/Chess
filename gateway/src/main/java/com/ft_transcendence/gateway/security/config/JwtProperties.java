package com.ft_transcendence.gateway.security.config;

import org.springframework.core.io.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.jwt")
public record JwtProperties(
        Resource privateKeyLocation,
        Resource publicKeyLocation
) {}