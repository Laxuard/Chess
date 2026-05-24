package com.ft_transcendence.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ft_transcendence.authservice.model.UserAuth;
import com.ft_transcendence.authservice.dto.UserMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ft_transcendence.authservice.service.AuthService;
import org.springframework.web.bind.annotation.RestController;
import com.ft_transcendence.authservice.dto.request.LoginRequest;
import com.ft_transcendence.authservice.dto.response.AuthResponse;
import com.ft_transcendence.authservice.dto.request.RegisterRequest;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {

        UserAuth savedUser = authService.register(request, session);
        AuthResponse response = userMapper.toRegisterResponse(savedUser);

        session.setAttribute("userId", savedUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {

        UserAuth loggedUser = authService.login(request, session);
        AuthResponse response = userMapper.toRegisterResponse(loggedUser);

        session.setAttribute("userId", loggedUser.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
