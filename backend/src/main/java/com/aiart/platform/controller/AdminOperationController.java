package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.AdminOperationDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.AdminOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operations")
@RequiredArgsConstructor
public class AdminOperationController {
    private final AdminOperationService adminOperationService;
    private final CurrentUser currentUser;

    @GetMapping
    public ApiResponse<AdminOperationDtos.OperationPage> operations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType) {
        currentUser.requireAdmin();
        return ApiResponse.ok(adminOperationService.operations(page, size, action, targetType));
    }
}
