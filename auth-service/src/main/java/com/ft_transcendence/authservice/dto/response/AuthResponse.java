package com.ft_transcendence.authservice.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;

public record AuthResponse(
        UUID userId,
        String username,
        Boolean is2FAEnabled
) {}