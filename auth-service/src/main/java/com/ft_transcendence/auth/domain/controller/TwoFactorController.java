package com.ft_transcendence.auth.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.dto.request.MfaVerificationRequest;
import com.ft_transcendence.auth.domain.service.twofactor.TwoFactorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @PostMapping("/setup")
    public ResponseEntity<MfaResponse> initiateMfaSetup(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        MfaResponse response = twoFactorService.initiateSetup(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable")
    public ResponseEntity<MfaResponse> finalizeMfaSetup(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        MfaResponse response = twoFactorService.finalizeSetup(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<MfaResponse> verifyLoginChallenge(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String sessionId = jwt.getClaimAsString("sid");
        UUID userId = UUID.fromString(jwt.getSubject());

        MfaResponse response = twoFactorService.verifyLoginChallenge(request, userId, sessionId);
        return ResponseEntity.ok(response);
    }
}