package com.ft_transcendence.social.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String username,
        String avatarUrl,
        String bio,
        boolean profileHidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
