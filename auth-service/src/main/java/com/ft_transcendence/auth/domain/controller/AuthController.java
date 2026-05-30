package com.ft_transcendence.auth.domain.controller;

import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.mapper.UserMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {

        UserAuth savedUser = authService.register(request, servletRequest);
        AuthResponse response = userMapper.toRegisterResponse(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        AuthStateResult stateResult = authService.login(request, servletRequest);

        AuthResponse response = userMapper.toLoginResponse(stateResult);

        if ("AWAITING_MFA".equals(stateResult.status())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/users")
// Secure method validation: Only users with the 'USER' role claim can execute this code!
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<String> getProfile(@AuthenticationPrincipal Jwt jwt) {

        // Extract the unique subject ID parsed from the Gateway token
        String userId = jwt.getSubject();

        // Extract any custom claims you added during the minting step
        String traceId = jwt.getClaimAsString("tid");

        return ResponseEntity.ok("Welcome back user: " + userId + " [Trace: " + traceId + "]");
    }

}
