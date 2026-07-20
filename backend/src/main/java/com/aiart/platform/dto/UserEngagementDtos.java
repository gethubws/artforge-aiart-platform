package com.aiart.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class UserEngagementDtos {
    private UserEngagementDtos() {
    }

    public record ToggleRequest(@NotBlank String targetType, @NotNull Long targetId) {
    }

    public record ToggleResult(String targetType, Long targetId, boolean active) {
    }

    public record TargetSummaryView(
            String targetType,
            Long targetId,
            String title,
            String summary,
            String coverImageUrl,
            String status,
            String ownerName,
            String routeView,
            String metaText,
            LocalDateTime targetUpdatedAt,
            LocalDateTime savedAt) {
    }

    public record NotificationView(
            Long id,
            String type,
            String title,
            String content,
            String relatedType,
            Long relatedId,
            String routeView,
            boolean read,
            LocalDateTime createdAt,
            LocalDateTime readAt) {
    }

    public record NotificationFeed(List<NotificationView> items, long unreadCount) {
    }
}
