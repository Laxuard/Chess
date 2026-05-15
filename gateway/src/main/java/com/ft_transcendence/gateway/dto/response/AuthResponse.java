package com.ft_transcendence.gateway.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String role,
        Boolean is2FAEnabled
) {}