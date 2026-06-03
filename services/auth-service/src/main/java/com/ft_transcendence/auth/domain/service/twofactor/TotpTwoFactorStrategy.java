package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;

import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeVerifier;

import org.springframework.stereotype.Component;

@Component
public class TotpTwoFactorStrategy implements TwoFactorStrategy {

    // Initialize the highly modular components provided by dev.samstevens.totp
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();

    // Sets up a default 30-second window with a ±1 step discrepancy tolerance cushion
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    @Override
    public MfaResponse.SetupDetails initiate(UserAuth user, UserTwoFactorMethod method) {
        // 1. Generate a modern, cryptographically secure 32-character Base32 secret string
        String secureSecret = secretGenerator.generate();
        method.setSecretKey(secureSecret);

        // 2. Build the standard multi-factor parameters data container mapping
        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secureSecret)
                .issuer("Ft_chess")
                .digits(6)
                .period(30)
                .build();

        String qrCodeUrl = qrData.getUri();

        // 4. Return the inner polymorphic data payload seamlessly
        return MfaResponse.SetupDetails.builder()
                .methodType(getType())
                .secretKey(secureSecret)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }

    @Override
    public boolean verify(UserTwoFactorMethod method, String code) {
        // Validation check: reject empty strings or inputs that aren't numeric blocks
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }

        // Executes code check directly on the record's secret row parameters data string
        return codeVerifier.isValidCode(method.getSecretKey(), code);
    }

    @Override
    public TwoFactorMethodType getType() {
        return TwoFactorMethodType.TOTP;
    }
}