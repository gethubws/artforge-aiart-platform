package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.UserEngagementDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.UserEngagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/engagement")
@RequiredArgsConstructor
public class UserEngagementController {
    private final UserEngagementService userEngagementService;
    private final CurrentUser currentUser;

    @PostMapping("/favorites/toggle")
    public ApiResponse<UserEngagementDtos.ToggleResult> toggleFavorite(
            @Valid @RequestBody UserEngagementDtos.ToggleRequest request) {
        return ApiResponse.ok(userEngagementService.toggleFavorite(currentUser.requireUserId(), request));
    }

    @PostMapping("/subscriptions/toggle")
    public ApiResponse<UserEngagementDtos.ToggleResult> toggleSubscription(
            @Valid @RequestBody UserEngagementDtos.ToggleRequest request) {
        return ApiResponse.ok(userEngagementService.toggleSubscription(currentUser.requireUserId(), request));
    }

    @GetMapping("/favorites")
    public ApiResponse<List<UserEngagementDtos.TargetSummaryView>> favorites(
            @RequestParam(required = false) String targetType) {
        return ApiResponse.ok(userEngagementService.favorites(currentUser.requireUserId(), targetType));
    }

    @GetMapping("/subscriptions")
    public ApiResponse<List<UserEngagementDtos.TargetSummaryView>> subscriptions(
            @RequestParam(required = false) String targetType) {
        return ApiResponse.ok(userEngagementService.subscriptions(currentUser.requireUserId(), targetType));
    }

    @GetMapping("/notifications")
    public ApiResponse<UserEngagementDtos.NotificationFeed> notifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "40") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ApiResponse.ok(userEngagementService.notifications(currentUser.requireUserId(), unreadOnly, page, size));
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ApiResponse<UserEngagementDtos.NotificationView> markRead(@PathVariable Long notificationId) {
        return ApiResponse.ok(userEngagementService.markRead(currentUser.requireUserId(), notificationId));
    }

    @PostMapping("/notifications/read-all")
    public ApiResponse<Map<String, Long>> markAllRead() {
        return ApiResponse.ok(Map.of("count", userEngagementService.markAllRead(currentUser.requireUserId())));
    }

    @GetMapping("/notifications/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.ok(Map.of("count", userEngagementService.unreadCount(currentUser.requireUserId())));
    }
}
