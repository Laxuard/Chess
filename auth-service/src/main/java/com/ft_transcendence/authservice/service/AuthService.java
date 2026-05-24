package com.ft_transcendence.authservice.service;

import com.ft_transcendence.authservice.dto.request.LoginRequest;
import com.ft_transcendence.authservice.exception.DuplicateResourceException;
import com.ft_transcendence.authservice.exception.ResourceNotFoundException;
import com.ft_transcendence.authservice.model.AuthProvider;
import com.ft_transcendence.authservice.model.UserIdentity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.ft_transcendence.authservice.model.UserAuth;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ft_transcendence.authservice.dto.request.RegisterRequest;
import com.ft_transcendence.authservice.repository.UserAuthRepository;

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

        List<String> roleStrings = savedUser.getRoles().stream()
                .map(Enum::name)
                .toList();

        session.setAttribute("userId", savedUser.getUserId());
        session.setAttribute("roles", roleStrings);

        // broadcast later before finishing.

        return savedUser;
    }

    @Transactional
    public UserAuth login(LoginRequest request, HttpSession session) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        authenticationManager.authenticate(authentication);

        UserAuth userAuth = userAuthRepository.findByEmailOrUsername(request.login(), request.login())
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        List<String> roleStrings = userAuth.getRoles().stream()
                .map(Enum::name)
                .toList();

        session.setAttribute("userId", userAuth.getUserId());
        session.setAttribute("roles", roleStrings);

        return userAuth;
    }

}
