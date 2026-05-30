package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.core.exception.InvalidCredentialsException;
import com.ft_transcendence.auth.core.exception.MfaException;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
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

    // TARGET CONCRETE MATCH: Directly autowires the actual underlying bean instance
    private final RedisIndexedSessionRepository sessionRepository;

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
            throw new MfaException("The multi-factor configuration option " + type + " is already verified active on your account.");
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
                .orElseThrow(() -> new MfaException("No pending configuration initialization sequence found for type: " + type + ". Run the /setup endpoint first."));

        boolean isValid = strategyFactory.getStrategy(type).verify(methodContext, request.code());

        if (!isValid) {
            throw new InvalidCredentialsException("The verification challenge code supplied is incorrect or has expired.");
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
    public MfaResponse verifyLoginChallenge(MfaVerificationRequest request, Jwt jwt) {
        UserAuth user = getUserFromJwt(jwt);
        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = user.getTwoFactorMethods().stream()
                .filter(m -> m.getMethodType() == type && m.isVerified())
                .findFirst()
                .orElseThrow(() -> new MfaException("No active verified " + type + " security mechanism is configured for this account."));

        boolean isValid = strategyFactory.getStrategy(type).verify(methodContext, request.code());

        if (!isValid) {
            throw new InvalidCredentialsException("MFA step-up verification validation failed. Access denied.");
        }

        methodContext.setLastUsedAt(LocalDateTime.now());
        userAuthRepository.save(user);

        String originalSessionId = jwt.getClaimAsString("sid");
        if (originalSessionId == null || originalSessionId.isBlank()) {
            throw new MfaException("Session reference context pointer ('sid') claim missing from Transit JWT schema boundaries.");
        }

        // 2. Fetch the session directly out of the shared Redis cluster database pool
        RedisIndexedSessionRepository.RedisSession originalSession = sessionRepository.findById(originalSessionId);
        if (originalSession == null) {
            throw new ResourceNotFoundException("Original authentication session tracking context");
        }

        // 3. Update the state value to clear your edge Gateway filtering constraints
        originalSession.setAttribute("isFullyAuthenticated", true);

        // 4. Flush the changes to the persistence layer
        sessionRepository.save(originalSession);

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