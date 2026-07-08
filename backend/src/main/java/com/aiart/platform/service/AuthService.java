package com.aiart.platform.service;

import com.aiart.platform.dto.AuthDtos;

public interface AuthService {
    AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request);

    AuthDtos.AuthResponse bootstrapAdmin(AuthDtos.RegisterRequest request);

    AuthDtos.AuthResponse login(AuthDtos.LoginRequest request);

    AuthDtos.UserProfile currentUser(Long userId);
}
