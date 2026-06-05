package com.ft_transcendence.auth.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.core.exception.BadRequestException;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.domain.repository.UserIdentityRepository;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;

import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserAuthRepository userAuthRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;


    /**
     * Authenticates an existing social user or auto-provisions a new shell account.
     */
    @Transactional
    public OAuth2UserSummary syncUser(OAuth2SyncRequest request) {
        return userIdentityRepository.findByProviderAndProviderId(request.provider(), request.providerId())
                .map(identity -> {
                    identity.setLastLoginAt(LocalDateTime.now());
                    UserAuth user = identity.getUser();
                    boolean modified = false;

                    // Sync avatar URL safely
                    String currentAvatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : "";
                    boolean hasDefaultPfp = "/assets/avatars/default-placeholder.png".equals(currentAvatar) || currentAvatar.isEmpty();
                    boolean currentlyUsingSocialPfp = currentAvatar.contains("googleusercontent.com") 
                                                    || currentAvatar.contains("intra.42.fr");
                    boolean pfpHasChangedOnSocialProvider = !request.avatarUrl().equals(currentAvatar);

                    if (hasDefaultPfp || (currentlyUsingSocialPfp && pfpHasChangedOnSocialProvider)) {
                        user.setAvatarUrl(request.avatarUrl());
                        modified = true;
                    }

                    // Sync email safely if changed on OAuth provider and not taken
                    if (!request.email().equalsIgnoreCase(user.getEmail())) {
                        if (!userAuthRepository.existsByEmail(request.email())) {
                            log.info("Updating email for user UUID [{}] from [{}] to [{}] based on OAuth provider update", 
                                    user.getUserId(), user.getEmail(), request.email());
                            user.setEmail(request.email());
                            modified = true;
                        } else {
                            log.warn("OAuth provider returned a new email [{}] for user UUID [{}], but it is already taken. Retaining current email [{}].",
                                    request.email(), user.getUserId(), user.getEmail());
                        }
                    }

                    if (modified) {
                        UserAuth saved = userAuthRepository.save(user);
                        eventPublisher.publishEvent(new com.ft_transcendence.common.event.UserSyncEvent(
                                saved.getUserId(),
                                saved.getUsername(),
                                saved.getEmail(),
                                saved.getAvatarUrl(),
                                saved.getVersion()
                        ));
                    }

                    return OAuth2UserSummary.fromEntity(user);
                })
                .orElseGet(() -> autoRegisterUser(request));
    }

    /**
     * Links a new social provider identity to an already authenticated local account context.
     */
    @Transactional
    public void linkAccount(UUID userId, AuthProvider provider, String providerId) {
        UserAuth user = fetchUserAuth(userId);
        validateNewLinkEligible(user, provider, providerId);

        UserIdentity newIdentity = UserIdentity.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .lastLoginAt(LocalDateTime.now())
                .build();

        user.addIdentity(newIdentity);
        userAuthRepository.save(user);
        log.info("Successfully linked external provider [{}] to user UUID [{}]", provider, userId);
    }

    /**
     * severs an external identity association from an account while preventing lockouts.
     */
    @Transactional
    public void unlinkAccount(UUID userId, AuthProvider provider) {
        UserAuth user = fetchUserAuth(userId);
        validateUnlinkSafe(user, provider);

        UserIdentity targetIdentity = user.getIdentities().stream()
                .filter(id -> id.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Linked provider identity"));

        user.getIdentities().remove(targetIdentity);
        userIdentityRepository.delete(targetIdentity);
        userAuthRepository.save(user);
        log.info("Successfully unlinked external provider [{}] from user UUID [{}]", provider, userId);
    }

    // ── PRIVATE DOMAIN LOGIC EXTRACTIONS ────────────────────────────────────

    private UserAuth fetchUserAuth(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    private OAuth2UserSummary autoRegisterUser(OAuth2SyncRequest request) {
        if (userAuthRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email address already exists via local credentials.");
        }

        UserAuth newAccount = UserAuth.builder()
                .username(generateUniqueUsername(request.name()))
                .email(request.email())
                .avatarUrl(request.avatarUrl())
                .build();

        UserIdentity socialIdentity = UserIdentity.builder()
                .user(newAccount)
                .provider(request.provider())
                .providerId(request.providerId())
                .lastLoginAt(LocalDateTime.now())
                .build();

        newAccount.addIdentity(socialIdentity);
        UserAuth saved = userAuthRepository.save(newAccount);
        
        eventPublisher.publishEvent(new com.ft_transcendence.common.event.UserSyncEvent(
                saved.getUserId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getAvatarUrl(),
                saved.getVersion()
        ));

        return OAuth2UserSummary.fromEntity(saved);
    }

    private String generateUniqueUsername(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            rawName = "user";
        }
        // Retain only alphanumeric characters and spaces, then format to underscore lowercase
        String baseUsername = rawName.replaceAll("[^a-zA-Z0-9\\s]", "")
                                    .trim()
                                    .replaceAll("\\s+", "_")
                                    .toLowerCase();
        if (baseUsername.isEmpty()) {
            baseUsername = "user";
        }
        if (!userAuthRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }
        return baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
    }

    private void validateNewLinkEligible(UserAuth user, AuthProvider provider, String providerId) {
        if (provider == AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot manually link LOCAL credentials.");
        }

        boolean identityTaken = userIdentityRepository.findByProviderAndProviderId(provider, providerId).isPresent();
        if (identityTaken) {
            throw new DuplicateResourceException("This " + provider + " account is already linked to another user.");
        }

        boolean alreadyLinked = user.getIdentities().stream().anyMatch(id -> id.getProvider() == provider);
        if (alreadyLinked) {
            throw new BadRequestException("You have already linked a " + provider + " account.");
        }
    }

    private void validateUnlinkSafe(UserAuth user, AuthProvider provider) {
        if (provider == AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot unlink LOCAL credentials.");
        }

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();
        if (!hasPassword && user.getIdentities().size() <= 1) {
            throw new BadRequestException("Cannot unlink this provider. You must first set up a local password or link another social login to prevent account lockout.");
        }
    }
}
