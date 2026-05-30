package com.ft_transcendence.auth.domain.controller;

import com.ft_transcendence.auth.domain.service.OAuth2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;

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

}
