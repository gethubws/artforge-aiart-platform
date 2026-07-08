package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.PointDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;
    private final CurrentUser currentUser;

    @GetMapping("/account")
    public ApiResponse<PointDtos.AccountView> account(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(pointService.account(currentUser.requireUserId(), page, size));
    }

    @PostMapping("/daily-claim")
    public ApiResponse<PointDtos.AccountView> claimDaily() {
        return ApiResponse.ok(pointService.claimDaily(currentUser.requireUserId()));
    }
}
