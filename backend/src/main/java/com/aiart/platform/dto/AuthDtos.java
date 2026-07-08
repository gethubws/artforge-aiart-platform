package com.aiart.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 6, max = 80) String password,
            @NotBlank @Size(max = 80) String displayName) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record AuthResponse(String token, UserProfile user) {
    }

    public record UserProfile(Long id, String username, String displayName, String avatarUrl, String role) {
    }
}
