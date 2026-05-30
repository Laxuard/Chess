package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record OAuth2UserSummary(
        UUID userId,
        List<String> roles
) {}