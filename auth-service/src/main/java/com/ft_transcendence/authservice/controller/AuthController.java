package com.ft_transcendence.authservice.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ft_transcendence.authservice.model.UserAuth;
import com.ft_transcendence.authservice.dto.UserMapper;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ft_transcendence.authservice.service.AuthService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ft_transcendence.authservice.dto.response.AuthResponse;
import com.ft_transcendence.authservice.dto.request.RegisterRequest;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        UserAuth savedUser = authService.register(request);

        session.setAttribute("userId", savedUser.getUserId().toString());

        session.setAttribute(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                savedUser.getUsername()
        );


        AuthResponse response = userMapper.toAuthResponse(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
