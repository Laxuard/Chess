package com.ft_transcendence.auth.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;
import com.ft_transcendence.auth.domain.repository.UserIdentityRepository;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserAuthRepository userAuthRepository;
    private final UserIdentityRepository userIdentityRepository;

    @Transactional
    public OAuth2UserSummary syncUser(OAuth2SyncRequest request) {
        return userIdentityRepository.findByProviderAndProviderId(request.provider(), request.providerId())
                .map(identity -> {

                    UserAuth user = identity.getUser();

                    identity.setLastLoginAt(LocalDateTime.now());

                    return convertToSummary(user);
                })
                .orElseGet(() -> autoRegisterUser(request));
    }

    /**
     * Private Provisioner: Creates a fresh UserAuth shell and hooks up the Social Identity.
     */
    private OAuth2UserSummary autoRegisterUser(OAuth2SyncRequest request) {
        if (userAuthRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email address already exists via local credentials.");
        }

        String uniqueUsername = request.name().replaceAll("\\s+", "_").toLowerCase();
        if (userAuthRepository.existsByUsername(uniqueUsername)) {
            uniqueUsername += "_" + UUID.randomUUID().toString().substring(0, 5);
        }

        UserAuth newAccount = UserAuth.builder()
                .username(uniqueUsername)
                .email(request.email())
                .build();

        UserIdentity socialIdentity = UserIdentity.builder()
                .user(newAccount)
                .provider(request.provider())
                .providerId(request.providerId())
                .lastLoginAt(LocalDateTime.now())
                .build();

        newAccount.addIdentity(socialIdentity);
        UserAuth savedAccount = userAuthRepository.save(newAccount);

        return convertToSummary(savedAccount);
    }

    /**
     * Dry Helper: Extracts exactly what the Gateway needs to map to Redis
     */
    private OAuth2UserSummary convertToSummary(UserAuth user) {
        List<String> roleStrings = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return OAuth2UserSummary.builder()
                .userId(user.getUserId())
                .roles(roleStrings)
                .build();
    }

}
