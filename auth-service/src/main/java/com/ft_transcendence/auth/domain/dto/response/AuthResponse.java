package com.ft_transcendence.auth.domain.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        Boolean is2FAEnabled
) {}