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
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.core.exception.BadRequestException;
import com.ft_transcendence.auth.domain.model.AuthProvider;
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

    /**
     * Link an authenticated account to a new social provider.
     */
    @Transactional
    public void linkAccount(UUID userId, AuthProvider provider, String providerId) {
        UserAuth user = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (provider == AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot manually link LOCAL credentials.");
        }

        boolean alreadyClaimed = userIdentityRepository
                .findByProviderAndProviderId(provider, providerId)
                .isPresent();
        if (alreadyClaimed) {
            throw new DuplicateResourceException("This " + provider + " account is already linked to another user.");
        }

        boolean alreadyLinked = user.getIdentities().stream()
                .anyMatch(id -> id.getProvider() == provider);
        if (alreadyLinked) {
            throw new BadRequestException("You have already linked a " + provider + " account.");
        }

        UserIdentity newIdentity = UserIdentity.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .lastLoginAt(LocalDateTime.now())
                .build();

        user.addIdentity(newIdentity);
        userAuthRepository.save(user);
    }

    /**
     * Unlink a social provider from an account.
     */
    @Transactional
    public void unlinkAccount(UUID userId, AuthProvider provider) {
        UserAuth user = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (provider == AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot unlink LOCAL credentials.");
        }

        UserIdentity targetIdentity = user.getIdentities().stream()
                .filter(id -> id.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Linked provider identity"));

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();
        long activeSocialIdentities = user.getIdentities().size();

        if (!hasPassword && activeSocialIdentities <= 1) {
            throw new BadRequestException("Cannot unlink this provider. You must first set up a local password or link another social login to prevent account lockout.");
        }

        user.getIdentities().remove(targetIdentity);
        userIdentityRepository.delete(targetIdentity);
        userAuthRepository.save(user);
    }

}
