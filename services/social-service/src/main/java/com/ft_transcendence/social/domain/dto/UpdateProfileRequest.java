package com.ft_transcendence.social.domain.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio,
        
        Boolean profileHidden
) {}
