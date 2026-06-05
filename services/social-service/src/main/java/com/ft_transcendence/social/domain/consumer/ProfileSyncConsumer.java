package com.ft_transcendence.social.domain.consumer;

import com.ft_transcendence.common.event.UserSyncEvent;
import com.ft_transcendence.common.event.UserDeleteEvent;
import com.ft_transcendence.social.domain.model.Profile;
import com.ft_transcendence.social.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileSyncConsumer {

    private final ProfileRepository profileRepository;

    @Transactional
    @KafkaListener(topics = "user-sync-topic", groupId = "social-profile-sync-group")
    public void consumeUserSync(UserSyncEvent event) {
        log.info("Received UserSyncEvent from Kafka: userId={}, username={}", event.userId(), event.username());
        
        profileRepository.findByUserId(event.userId())
                .ifPresentOrElse(
                        profile -> {
                            // Compare directly against our manual auth-timeline version tracking column
                            if (event.version() >= profile.getSyncVersion()) {
                                log.info("Updating existing profile for userId: {}. Old Sync Version: {}, New Event version: {}", 
                                        event.userId(), profile.getSyncVersion(), event.version());
                                profile.setUsername(event.username());
                                profile.setAvatarUrl(event.avatarUrl());
                                profile.setSyncVersion(event.version()); // Lock down the new sequence checkpoint
                                profileRepository.save(profile);
                            } else {
                                log.warn("Skipping outdated UserSyncEvent. Stored sync version: {}, Event version: {}", 
                                        profile.getSyncVersion(), event.version());
                            }
                        },
                        () -> {
                            log.info("Creating new profile for userId: {}", event.userId());
                            Profile newProfile = Profile.builder()
                                    .userId(event.userId())
                                    .username(event.username())
                                    .avatarUrl(event.avatarUrl())
                                    .syncVersion(event.version()) // Establish the genesis version marker
                                    .build();
                            profileRepository.save(newProfile);
                        }
                );
    }

    @Transactional
    @KafkaListener(topics = "user-delete-topic", groupId = "social-profile-sync-group")
    public void consumeUserDelete(UserDeleteEvent event) {
        log.info("Received UserDeleteEvent from Kafka: userId={}", event.userId());
        
        profileRepository.findByUserId(event.userId())
                .ifPresentOrElse(
                        profile -> {
                            log.info("Deleting profile for userId: {}", event.userId());
                            profileRepository.delete(profile);
                        },
                        () -> log.warn("Profile not found for deletion, userId: {}", event.userId())
                );
    }
}
