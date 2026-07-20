package com.aiart.platform.service;

import com.aiart.platform.dto.AdminUserDtos;

public interface AdminUserService {
    AdminUserDtos.UserPage users(int page, int size, String keyword, String role, String status);

    AdminUserDtos.UserRow updateUser(Long operatorId, Long userId, AdminUserDtos.UserUpdateRequest request);
}
