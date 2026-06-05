package com.ft_transcendence.social.domain.controller;

import com.ft_transcendence.social.domain.dto.ProfileResponse;
import com.ft_transcendence.social.domain.dto.UpdateProfileRequest;
import com.ft_transcendence.social.domain.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getProfileByUsername(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String username) {
        UUID requesterUserId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(profileService.getProfileByUsername(username, requesterUserId));
    }

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getPublicProfiles() {
        return ResponseEntity.ok(profileService.getPublicProfiles());
    }
}
