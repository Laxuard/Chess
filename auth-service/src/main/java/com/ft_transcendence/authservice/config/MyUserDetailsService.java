package com.ft_transcendence.authservice.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import com.ft_transcendence.authservice.repository.UserAuthRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

@Service

@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String identifier) throws UsernameNotFoundException {
        try {
            UUID userId = UUID.fromString(identifier);
            return userAuthRepository.findByUserId(userId)
                    .map(SecurityUser::new)
                    .orElseThrow(() -> new UsernameNotFoundException(identifier));

        } catch (IllegalArgumentException ex) {
            return userAuthRepository.findByEmailOrUsername(identifier, identifier)
                    .map(SecurityUser::new)
                    .orElseThrow(() -> new UsernameNotFoundException(identifier));
        }
    }
}
