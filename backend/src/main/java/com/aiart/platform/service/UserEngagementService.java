package com.aiart.platform.service;

import com.aiart.platform.dto.UserEngagementDtos;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserEngagementService {
    UserEngagementDtos.ToggleResult toggleFavorite(Long userId, UserEngagementDtos.ToggleRequest request);

    UserEngagementDtos.ToggleResult toggleSubscription(Long userId, UserEngagementDtos.ToggleRequest request);

    List<UserEngagementDtos.TargetSummaryView> favorites(Long userId, String targetType);

    List<UserEngagementDtos.TargetSummaryView> subscriptions(Long userId, String targetType);

    UserEngagementDtos.NotificationFeed notifications(Long userId, boolean unreadOnly, int page, int size);

    UserEngagementDtos.NotificationView markRead(Long userId, Long notificationId);

    long markAllRead(Long userId);

    long unreadCount(Long userId);

    Set<Long> subscriptionUserIds(String targetType, Long targetId);

    void notifyUsers(Collection<Long> userIds, Long actorUserId, String type, String title, String content,
                     String relatedType, Long relatedId);
}
