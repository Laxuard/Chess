package com.ft_transcendence.auth.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.security.core.Authentication;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import com.ft_transcendence.auth.security.context.SecurityUser;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import org.springframework.security.authentication.AuthenticationManager;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserAuthRepository userAuthRepository;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new account shell along with its initial LOCAL credentials profile.
     */
    @Transactional
    public UserAuth register(RegisterRequest request) {
        validateRegistrationUnique(request.username(), request.email());

        UserAuth userAuth = UserAuth.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        UserIdentity localIdentity = UserIdentity.builder()
                .user(userAuth)
                .provider(AuthProvider.LOCAL)
                .providerId(request.email())
                .lastLoginAt(LocalDateTime.now())
                .build();

        userAuth.addIdentity(localIdentity);
        
        return userAuthRepository.save(userAuth);
    }

    /**
     * Executes the primary credentials verification challenge sequence against the security manager.
     */
    @Transactional(readOnly = true)
    public AuthStateResult login(LoginRequest request) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        Authentication authResult = authenticationManager.authenticate(authenticationToken);

        SecurityUser securityUser = (SecurityUser) authResult.getPrincipal();
        UserAuth userAuth = securityUser.userAuth();

        if (userAuth.is2faEnabled()) {
            List<TwoFactorMethodType> verifiedMethods = userAuth.getTwoFactorMethods().stream()
                    .filter(UserTwoFactorMethod::isVerified)
                    .map(UserTwoFactorMethod::getMethodType)
                    .toList();

            return new AuthStateResult("AWAITING_MFA", userAuth, verifiedMethods);
        }

        return new AuthStateResult("AUTHENTICATED", userAuth, List.of());
    }

    /**
     * Fetches details for a unique user ID.
     */
    @Transactional(readOnly = true)
    public UserAuth getUserDetails(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    // ── PRIVATE GUARD BLOCKS ────────────────────────────────────────────────

    private void validateRegistrationUnique(String username, String email) {
        if (userAuthRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userAuthRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }
    }
}
