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
                            Integer currentVersion = profile.getVersion();
                            long currentVerVal = currentVersion != null ? currentVersion.longValue() : 0L;
                            if (event.version() >= currentVerVal) {
                                log.info("Updating existing profile for userId: {}. Stored version: {}, Event version: {}", 
                                        event.userId(), currentVerVal, event.version());
                                profile.setUsername(event.username());
                                profile.setAvatarUrl(event.avatarUrl());
                                profileRepository.save(profile);
                            } else {
                                log.warn("Skipping outdated UserSyncEvent. Stored version: {}, Event version: {}", 
                                        currentVerVal, event.version());
                            }
                        },
                        () -> {
                            log.info("Creating new profile for userId: {}", event.userId());
                            Profile newProfile = Profile.builder()
                                    .userId(event.userId())
                                    .username(event.username())
                                    .avatarUrl(event.avatarUrl())
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
