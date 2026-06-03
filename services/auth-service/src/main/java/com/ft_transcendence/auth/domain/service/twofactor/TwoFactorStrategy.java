package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

public interface TwoFactorStrategy {

    /**
     * Orchestrates the initialization channel requirements for this specific 2FA method type.
     * Stuffs its unique tracking criteria directly into the relational method entity row.
     * Returns ONLY the custom setup payload configuration details (e.g. backup arrays, keys, etc.).
     */
    MfaResponse.SetupDetails initiate(UserAuth user, UserTwoFactorMethod method);

    /**
     * Verifies the user-supplied challenge code.
     */
    boolean verify(UserTwoFactorMethod method, String code);

    /**
     * Returns the strongly typed Enum variant handled by this strategy.
     */
    TwoFactorMethodType getType();
}