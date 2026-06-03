package com.ft_transcendence.auth.domain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.service.OAuth2Service;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.request.OAuth2LinkRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;
import com.ft_transcendence.auth.domain.dto.request.OAuth2UnlinkRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    /**
     * Internal Back channel Synchronization.
     * Invoked stateless by the Gateway to find-or-create anonymous SSO sign-ins.
     */
    @PostMapping("/sync")
    public ResponseEntity<OAuth2UserSummary> syncOAuth2Users(@Valid @RequestBody OAuth2SyncRequest request) {

        OAuth2UserSummary summary = oauth2Service.syncUser(request);
        return ResponseEntity.ok(summary);
    }

    /**
     * Account Linking Endpoint.
     * Leverages the active authenticated principal context to link an identity safely.
     */
    @PostMapping("/link")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<Void> linkSocialAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OAuth2LinkRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        oauth2Service.linkAccount(userId, request.provider(), request.providerId());

        return ResponseEntity.ok().build();
    }

    /**
     * Account Unlinking Endpoint.
     * Severs a social identity link safely using the token context.
     */
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