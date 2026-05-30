package com.ft_transcendence.auth.security.context;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service

@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String loginIdentifier) throws UsernameNotFoundException {
        return userAuthRepository.findByEmailOrUsername(loginIdentifier, loginIdentifier)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("No identity credentials found matching identifier: %s", loginIdentifier)
                ));
    }
}
