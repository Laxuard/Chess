package com.ft_transcendence.auth.domain.dto;

import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

import java.util.List;

public record AuthStateResult(
        String status,
        UserAuth user,
        List<TwoFactorMethodType> availableMethods
) {}