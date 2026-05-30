package com.ft_transcendence.auth.domain.controller;

import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.ft_transcendence.auth.domain.dto.request.MfaVerificationRequest;
import com.ft_transcendence.auth.domain.service.twofactor.TwoFactorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @PostMapping("/setup")
    public ResponseEntity<MfaResponse> initiateMfaSetup(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(twoFactorService.initiateSetup(request, jwt));
    }

    @PostMapping("/enable")
    public ResponseEntity<MfaResponse> finalizeMfaSetup(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(twoFactorService.finalizeSetup(request, jwt));
    }

    @PostMapping("/verify")
    public ResponseEntity<MfaResponse> verifyLoginChallenge(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(twoFactorService.verifyLoginChallenge(request, jwt));
    }
}