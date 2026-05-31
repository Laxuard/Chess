package com.ft_transcendence.auth.domain.service;

import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import com.ft_transcendence.auth.domain.model.*;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import com.ft_transcendence.auth.security.context.SecurityUser;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import org.springframework.security.authentication.AuthenticationManager;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserAuthRepository userAuthRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserAuth register(RegisterRequest request, jakarta.servlet.http.HttpServletRequest servletRequest) {
        if (userAuthRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userAuthRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists");
        }

        UserAuth userAuth = UserAuth.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        UserIdentity userIdentity = UserIdentity.builder()
                .user(userAuth)
                .provider(AuthProvider.LOCAL)
                .providerId(request.email())
                .lastLoginAt(LocalDateTime.now())
                .build();

        userAuth.addIdentity(userIdentity);
        UserAuth savedUser = userAuthRepository.save(userAuth);

        HttpSession session = servletRequest.getSession(true);
        syncSessionAttributes(savedUser, session);

        // broadcast later before finishing.

        return savedUser;
    }

    @Transactional(readOnly = true)
    public AuthStateResult login(LoginRequest request, jakarta.servlet.http.HttpServletRequest servletRequest) {
        // Submit credentials to the authentication manager layer
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        Authentication authResult = authenticationManager.authenticate(authenticationToken);

        SecurityUser securityUser = (SecurityUser) authResult.getPrincipal();
        UserAuth userAuth = securityUser.userAuth();

        HttpSession session = servletRequest.getSession(true);
        syncSessionAttributes(userAuth, session);
        if (userAuth.is2faEnabled()) {
            List<TwoFactorMethodType> methods = userAuth.getTwoFactorMethods().stream()
                    .filter(UserTwoFactorMethod::isVerified)
                    .map(UserTwoFactorMethod::getMethodType)
                    .toList();

            return new AuthStateResult("AWAITING_MFA", userAuth, methods);
        }

        return new AuthStateResult("AUTHENTICATED", userAuth, List.of());
    }

    private void syncSessionAttributes(UserAuth user, HttpSession session) {
        List<String> roleStrings = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        session.setAttribute("roles", roleStrings);
        session.setAttribute("userId", user.getUserId());

        if (user.is2faEnabled()) {
            session.setAttribute("isFullyAuthenticated", false);
        } else {
            session.setAttribute("isFullyAuthenticated", true);
        }

    }

    @Transactional(readOnly = true)
    public UserAuth getUserDetails(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new com.ft_transcendence.auth.core.exception.ResourceNotFoundException("User not found with ID: " + userId));
    }

}
