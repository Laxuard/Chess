package com.ft_transcendence.gateway.config;

import org.springframework.core.io.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Resource privateKeyPath,
        Resource publicKeyPath
) {}