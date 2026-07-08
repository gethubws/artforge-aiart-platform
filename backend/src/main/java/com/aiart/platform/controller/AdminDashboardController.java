package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.AdminDashboardDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;
    private final CurrentUser currentUser;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardDtos.DashboardView> dashboard() {
        currentUser.requireAdmin();
        return ApiResponse.ok(adminDashboardService.dashboard());
    }
}
