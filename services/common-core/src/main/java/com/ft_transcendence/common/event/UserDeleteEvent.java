package com.ft_transcendence.common.event;

import java.io.Serializable;
import java.util.UUID;

/**
 * Event published when a user account is permanently deleted or anonymized.
 * Consumed by downstream microservices to clean up dependent profiles, chat records, and match histories.
 */
public record UserDeleteEvent(
        UUID userId
) implements Serializable {}
