package com.ft_transcendence.authservice.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;

public record RegisterResponse(
        UUID userId,
        String username,
        String email,
        String role,
        LocalDateTime createdAt,
        String message
) {}