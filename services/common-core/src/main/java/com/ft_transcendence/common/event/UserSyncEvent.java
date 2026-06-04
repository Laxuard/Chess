package com.ft_transcendence.common.event;

import java.io.Serializable;
import java.util.UUID;

/**
 * Event published when user core profile details (username, email, avatar) are created or updated.
 * Consumed by downstream microservices (like social-service) to keep their databases in sync.
 */
public record UserSyncEvent(
        UUID userId,
        String username,
        String email,
        String avatarUrl,
        long version
) implements Serializable {}
