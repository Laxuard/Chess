package com.ft_transcendence.social.domain.service;

import com.ft_transcendence.social.domain.dto.ProfileResponse;
import com.ft_transcendence.social.domain.dto.UpdateProfileRequest;
import com.ft_transcendence.social.domain.model.Profile;
import com.ft_transcendence.social.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponse getProfileByUserId(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return mapToResponse(profile);
    }

    public ProfileResponse getProfileByUsername(String username, UUID requesterUserId) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (profile.isProfileHidden() && !profile.getUserId().equals(requesterUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This profile is private");
        }

        return mapToResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.profileHidden() != null) {
            profile.setProfileHidden(request.profileHidden());
        }

        Profile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    public List<ProfileResponse> getPublicProfiles() {
        return profileRepository.findAll().stream()
                .filter(p -> !p.isProfileHidden())
                .map(this::mapToResponse)
                .toList();
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return new ProfileResponse(
                profile.getUserId(),
                profile.getUsername(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.isProfileHidden(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
