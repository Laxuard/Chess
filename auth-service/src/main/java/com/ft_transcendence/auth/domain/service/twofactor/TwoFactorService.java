package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.core.exception.InvalidCredentialsException;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.TwoFactorMethodType;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.model.UserTwoFactorMethod;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import com.ft_transcendence.auth.domain.dto.request.MfaVerificationRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserAuthRepository userAuthRepository;
    private final TwoFactorStrategyFactory strategyFactory;

    /**
     * Phase 1: Initiates a new pending multi-factor registration context flow.
     * Passes raw objects to strategies and binds responses into universal envelopes.
     */
    @Transactional
    public MfaResponse initiateSetup(MfaVerificationRequest request, Jwt jwt) {
        UserAuth user = getUserFromJwt(jwt);
        TwoFactorMethodType type = request.methodType();

        boolean alreadyExists = user.getTwoFactorMethods().stream()
                .anyMatch(m -> m.getMethodType() == type && m.isVerified());
        if (alreadyExists) {
            throw new IllegalStateException("The multi-factor configuration option " + type + " is already active.");
        }

        user.getTwoFactorMethods().removeIf(m -> m.getMethodType() == type && !m.isVerified());

        UserTwoFactorMethod pendingMethod = UserTwoFactorMethod.builder()
                .methodType(type)
                .isVerified(false)
                .build();

        // 1. Invoke the strategy engine to populate custom row metadata and return specific payload details
        MfaResponse.SetupDetails dynamicDetails = strategyFactory.getStrategy(type).initiate(user, pendingMethod);

        user.addTwoFactorMethod(pendingMethod);
        userAuthRepository.save(user);

        // 2. Wrap the dynamic strategy payload cleanly inside our central, global envelope container
        return MfaResponse.builder()
                .status("SETUP_INITIATED")
                .message("Multi-factor setup verification challenge initiated successfully. Complete the verification handshake sequence to protect your account.")
                .setupDetails(dynamicDetails) // Passes through smoothly without hardcoding fields!
                .build();
    }

    /**
     * Phase 2: Finalizes linking a new 2FA channel after checking the initial code.
     */
    @Transactional
    public MfaResponse finalizeSetup(MfaVerificationRequest request, Jwt jwt) {
        UserAuth user = getUserFromJwt(jwt);
        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = user.getTwoFactorMethods().stream()
                .filter(m -> m.getMethodType() == type && !m.isVerified())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No pending configuration found for type: " + type));

        boolean isValid = strategyFactory.getStrategy(type).verify(methodContext, request.code());

        if (!isValid) {
            throw new InvalidCredentialsException("The initialization challenge code supplied is invalid or has expired.");
        }

        methodContext.setVerified(true);
        methodContext.setLastUsedAt(LocalDateTime.now());
        user.set2faEnabled(true);
        userAuthRepository.save(user);

        log.info("User [{}] successfully linked multi-factor channel: {}", user.getUsername(), type);

        return MfaResponse.builder()
                .status("ENABLED")
                .message("Multi-factor authentication option verified and permanently locked to your identity profile successfully.")
                .build();
    }

    /**
     * Phase 3: Validates a user's step-up login verification challenge code.
     */
    @Transactional
    public MfaResponse verifyLoginChallenge(MfaVerificationRequest request, HttpSession session) {
        Object userIdAttr = session.getAttribute("userId");
        if (userIdAttr == null) {
            throw new ResourceNotFoundException("No active half-logged session context available for challenge verification.");
        }

        UUID userId = UUID.fromString(userIdAttr.toString());
        UserAuth user = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User identity reference not found for ID: " + userId));

        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = user.getTwoFactorMethods().stream()
                .filter(m -> m.getMethodType() == type && m.isVerified())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active configuration verified for type: " + type));

        boolean isValid = strategyFactory.getStrategy(type).verify(methodContext, request.code());

        if (!isValid) {
            throw new InvalidCredentialsException("MFA step-up verification validation failed. Access denied.");
        }

        methodContext.setLastUsedAt(LocalDateTime.now());
        userAuthRepository.save(user);

        session.setAttribute("isFullyAuthenticated", true);
        log.info("User [{}] successfully cleared login challenge via: {}", user.getUsername(), type);

        return MfaResponse.builder()
                .status("VERIFIED")
                .message("MFA verification challenge cleared successfully. Edge gateway core microservice network proxy unblocked.")
                .build();
    }

    private UserAuth getUserFromJwt(Jwt jwt) {
        String userIdStr = jwt.getClaimAsString("sub");
        UUID userId = UUID.fromString(userIdStr);

        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for JWT subject: " + userIdStr));
    }
}