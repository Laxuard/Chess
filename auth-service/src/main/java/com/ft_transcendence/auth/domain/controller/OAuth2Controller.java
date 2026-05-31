package com.ft_transcendence.auth.domain.controller;

import com.ft_transcendence.auth.domain.service.OAuth2Service;
import com.ft_transcendence.auth.domain.dto.request.OAuth2LinkRequest;
import com.ft_transcendence.auth.domain.dto.request.OAuth2UnlinkRequest;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @PostMapping("/sync")
    public ResponseEntity<OAuth2UserSummary> syncOAuth2Users(@Valid @RequestBody OAuth2SyncRequest request) {
        OAuth2UserSummary summary = oauth2Service.syncUser(request);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/link")
    public ResponseEntity<Void> linkSocialAccount(@Valid @RequestBody OAuth2LinkRequest request) {
        oauth2Service.linkAccount(request.userId(), request.provider(), request.providerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlink")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<Void> unlinkSocialAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OAuth2UnlinkRequest request) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        oauth2Service.unlinkAccount(userId, request.provider());
        return ResponseEntity.ok().build();
    }

}
