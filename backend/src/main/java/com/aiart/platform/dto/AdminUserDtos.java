package com.aiart.platform.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminUserDtos {
    private AdminUserDtos() {
    }

    public record UserPage(List<UserRow> items,
                           long page,
                           long size,
                           long total,
                           long pages) {
    }

    public record UserRow(Long id,
                          String username,
                          String displayName,
                          String avatarUrl,
                          String role,
                          String status,
                          BigDecimal pointBalance,
                          long artworkCount,
                          long generationCount,
                          long stylePackageCount,
                          long taskCount,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
    }

    public record UserUpdateRequest(@Size(max = 80) String displayName,
                                    String role,
                                    String status) {
    }
}
