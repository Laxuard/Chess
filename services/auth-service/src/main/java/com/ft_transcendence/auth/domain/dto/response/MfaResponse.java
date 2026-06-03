package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Dynamically strips unpopulated null blocks out of the outbound TCP stream
public record MfaResponse(
        String status,                // "SETUP_INITIATED", "ENABLED", or "VERIFIED"
        String message,               // Human-readable localized transaction confirmation text
        SetupDetails setupDetails     // Multi-method setup configuration block wrapper
) {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SetupDetails(
            TwoFactorMethodType methodType,

            // === TOTP Authenticator Apps Channel Specifics ===
            String secretKey,
            String qrCodeUrl,

            // === Out-of-Band Delivery Channels (SMS / Email) Specifics ===
            String targetDestination,     // Holds a masked destination string (e.g., "+*******12" or "l******d@gmail.com")

            // === Backup Recovery Codes Channel Specifics ===
            List<String> backupCodes      // Array containing generated recovery hashes
    ) {}
}