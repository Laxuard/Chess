package com.ft_transcendence.auth.domain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.mapper.UserMapper;
import com.ft_transcendence.auth.domain.service.AuthService;
import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import com.ft_transcendence.auth.domain.dto.request.SetPasswordRequest;
import com.ft_transcendence.auth.domain.dto.response.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, 
            HttpServletRequest servletRequest) {

        UserAuth savedUser = authService.register(request);
        
        // Populate the active session properties directly inside the controller tier boundary
        HttpSession session = servletRequest.getSession(true);
        syncSessionAttributes(savedUser, session);

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toRegisterResponse(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletRequest servletRequest) {
        
        AuthStateResult stateResult = authService.login(request);

        HttpSession session = servletRequest.getSession(true);
        syncSessionAttributes(stateResult.user(), session);

        if ("AWAITING_MFA".equals(stateResult.status())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(userMapper.toLoginResponse(stateResult));
        }

        return ResponseEntity.ok(userMapper.toLoginResponse(stateResult));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserAuth userDetails = authService.getUserDetails(userId);
        
        return ResponseEntity.ok(userMapper.toProfileResponse(userDetails));
    }

    @PostMapping("/password/set")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<Void> setPassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SetPasswordRequest request) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        authService.setPassword(userId, request.password(), request.currentPassword());
        
        return ResponseEntity.ok().build();
    }

    // ── WEB SESSION HANDSHAKE SYNC ──────────────────────────────────────────

    private void syncSessionAttributes(UserAuth user, HttpSession session) {
        List<String> roleStrings = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        session.setAttribute("roles", roleStrings);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("isFullyAuthenticated", !user.is2faEnabled());
    }
}
