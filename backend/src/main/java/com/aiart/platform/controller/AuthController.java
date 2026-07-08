package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.AuthDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/register")
    public ApiResponse<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/bootstrap-admin")
    public ApiResponse<AuthDtos.AuthResponse> bootstrapAdmin(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ApiResponse.ok(authService.bootstrapAdmin(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthDtos.UserProfile> me() {
        return ApiResponse.ok(authService.currentUser(currentUser.requireUserId()));
    }
}
