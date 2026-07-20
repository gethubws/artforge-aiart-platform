package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.AdminUserDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;
    private final CurrentUser currentUser;

    @GetMapping
    public ApiResponse<AdminUserDtos.UserPage> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        currentUser.requireAdmin();
        return ApiResponse.ok(adminUserService.users(page, size, keyword, role, status));
    }

    @PutMapping("/{userId}")
    public ApiResponse<AdminUserDtos.UserRow> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserDtos.UserUpdateRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(adminUserService.updateUser(currentUser.requireUserId(), userId, request));
    }
}
