package com.ft_transcendence.auth.domain.service;

import com.ft_transcendence.auth.security.context.SecurityUser;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.security.core.Authentication;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import org.springframework.security.authentication.AuthenticationManager;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserAuthRepository userAuthRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserAuth register(RegisterRequest request, HttpSession session) {
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
                .build();

        userAuth.addIdentity(userIdentity);
        UserAuth savedUser = userAuthRepository.save(userAuth);

        syncSessionAttributes(savedUser, session);

        // broadcast later before finishing.

        return savedUser;
    }

    @Transactional(readOnly = true)
    public UserAuth login(LoginRequest request, HttpSession session) {
        // Submit credentials to the authentication manager layer
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        Authentication authResult = authenticationManager.authenticate(authenticationToken);

        SecurityUser securityUser = (SecurityUser) authResult.getPrincipal();
        UserAuth userAuth = securityUser.userAuth();

        syncSessionAttributes(userAuth, session);
        return userAuth;
    }

    private void syncSessionAttributes(UserAuth user, HttpSession session) {
        List<String> roleStrings = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("roles", roleStrings);
    }

}
