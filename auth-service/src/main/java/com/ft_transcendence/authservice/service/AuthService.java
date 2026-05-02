package com.ft_transcendence.authservice.service;

import com.ft_transcendence.authservice.exception.DuplicateResourceException;
import com.ft_transcendence.authservice.model.AuthProvider;
import com.ft_transcendence.authservice.model.UserIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ft_transcendence.authservice.model.UserAuth;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ft_transcendence.authservice.dto.request.RegisterRequest;
import com.ft_transcendence.authservice.repository.UserAuthRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserAuthRepository userAuthRepository;

    @Transactional
    public UserAuth register(RegisterRequest request)
    {
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

        // broadcast later before finishing.

        return userAuthRepository.save(userAuth);
    }

}
