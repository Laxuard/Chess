package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record SetPasswordRequest(
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters long and under 100 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
        )
        String password,

        String currentPassword
) {}
