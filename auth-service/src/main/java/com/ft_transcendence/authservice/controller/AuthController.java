package com.ft_transcendence.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ft_transcendence.authservice.model.UserAuth;
import com.ft_transcendence.authservice.dto.UserMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ft_transcendence.authservice.service.AuthService;
import org.springframework.web.bind.annotation.RestController;
import com.ft_transcendence.authservice.dto.request.RegisterRequest;
import com.ft_transcendence.authservice.dto.response.RegisterResponse;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserAuth savedUser = authService.register(request);

        RegisterResponse response = userMapper.toRegisterResponse(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
