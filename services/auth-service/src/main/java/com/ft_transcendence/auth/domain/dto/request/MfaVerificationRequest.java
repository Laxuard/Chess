package com.ft_transcendence.auth.domain.dto.request;

import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

public record MfaVerificationRequest(
        TwoFactorMethodType methodType,
        String code
) {}
