package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.core.exception.InvalidCredentialsException;
import com.ft_transcendence.auth.core.exception.MfaException;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.domain.dto.request.MfaVerificationRequest;
import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository.RedisSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserAuthRepository userAuthRepository;
    private final TwoFactorStrategyFactory strategyFactory;
    private final RedisIndexedSessionRepository sessionRepository;

    /**
     * Phase 1: Initiates a new pending multi-factor registration context flow.
     */
    @Transactional
    public MfaResponse initiateSetup(MfaVerificationRequest request, UUID userId) {
        UserAuth user = fetchUserAuth(userId);
        TwoFactorMethodType type = request.methodType();

        validateSetupEligible(user, type);

        // Clean out any historical stale, unverified challenge configurations of this type
        user.getTwoFactorMethods().removeIf(method -> method.getMethodType() == type && !method.isVerified());

        UserTwoFactorMethod pendingMethod = UserTwoFactorMethod.builder()
                .methodType(type)
                .isVerified(false)
                .build();

        MfaResponse.SetupDetails dynamicDetails = strategyFactory.getStrategy(type).initiate(user, pendingMethod);

        user.addTwoFactorMethod(pendingMethod);
        userAuthRepository.save(user);

        return MfaResponse.builder()
                .status("SETUP_INITIATED")
                .message("Multi-factor setup verification challenge initiated successfully. Complete the verification handshake sequence to protect your account.")
                .setupDetails(dynamicDetails)
                .build();
    }

    /**
     * Phase 2: Finalizes linking a new 2FA channel after verifying the initial code challenge.
     */
    @Transactional
    public MfaResponse finalizeSetup(MfaVerificationRequest request, UUID userId) {
        UserAuth user = fetchUserAuth(userId);
        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = fetchPendingMethod(user, type);
        verifyChallengeToken(methodContext, type, request.code());

        methodContext.setVerified(true);
        methodContext.setLastUsedAt(LocalDateTime.now());
        user.set2faEnabled(true);

        userAuthRepository.save(user);
        log.info("User [{}] successfully enabled multi-factor channel: {}", user.getUsername(), type);

        return MfaResponse.builder()
                .status("ENABLED")
                .message("Multi-factor authentication option verified and permanently locked to your identity profile successfully.")
                .build();
    }

    /**
     * Phase 3: Validates a user's step-up login verification challenge code and upgrades the active Redis session.
     */
    @Transactional
    public MfaResponse verifyLoginChallenge(MfaVerificationRequest request, UUID userId, String sessionId) {
        validateSessionIdPresent(sessionId);

        UserAuth user = fetchUserAuth(userId);
        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = fetchVerifiedMethod(user, type);
        verifyChallengeToken(methodContext, type, request.code());

        methodContext.setLastUsedAt(LocalDateTime.now());
        userAuthRepository.save(user);

        upgradeSharedRedisSession(sessionId);
        log.info("User [{}] successfully verified step-up login challenge via: {}", user.getUsername(), type);

        return MfaResponse.builder()
                .status("VERIFIED")
                .message("MFA verification challenge cleared successfully. Edge gateway core microservice network proxy unblocked.")
                .build();
    }

    // ── PRIVATE DOMAIN VALIDATIONS & LOOKUPS ─────────────────────────────────

    private UserAuth fetchUserAuth(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    private UserTwoFactorMethod fetchPendingMethod(UserAuth user, TwoFactorMethodType type) {
        return user.getTwoFactorMethods().stream()
                .filter(method -> method.getMethodType() == type && !method.isVerified())
                .findFirst()
                .orElseThrow(() -> new MfaException("No pending configuration initialization sequence found for type: " + type + ". Run the /setup endpoint first."));
    }

    private UserTwoFactorMethod fetchVerifiedMethod(UserAuth user, TwoFactorMethodType type) {
        return user.getTwoFactorMethods().stream()
                .filter(method -> method.getMethodType() == type && method.isVerified())
                .findFirst()
                .orElseThrow(() -> new MfaException("No active verified " + type + " security mechanism is configured for this account."));
    }

    private void verifyChallengeToken(UserTwoFactorMethod methodContext, TwoFactorMethodType type, String code) {
        boolean isValid = strategyFactory.getStrategy(type).verify(methodContext, code);
        if (!isValid) {
            throw new InvalidCredentialsException("The verification challenge code supplied is incorrect or has expired.");
        }
    }

    private void upgradeSharedRedisSession(String sessionId) {
        RedisSession originalSession = sessionRepository.findById(sessionId);
        if (originalSession == null) {
            throw new ResourceNotFoundException("Original authentication session tracking context");
        }

        originalSession.setAttribute("isFullyAuthenticated", true);
        sessionRepository.save(originalSession);
    }

    private void validateSetupEligible(UserAuth user, TwoFactorMethodType type) {
        boolean alreadyExists = user.getTwoFactorMethods().stream()
                .anyMatch(method -> method.getMethodType() == type && method.isVerified());
        if (alreadyExists) {
            throw new MfaException("The multi-factor configuration option " + type + " is already verified active on your account.");
        }
    }

    private void validateSessionIdPresent(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new MfaException("Session reference context pointer missing from structural transaction payload parameters.");
        }
    }
}
