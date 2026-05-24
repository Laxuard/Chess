package com.ft_transcendence.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Login (Email or Username) is required") String login,

        @NotBlank(message = "Password is required") String password) {
}