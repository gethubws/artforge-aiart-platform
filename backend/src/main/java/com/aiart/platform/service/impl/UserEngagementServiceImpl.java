package com.aiart.platform.service.impl;

import com.aiart.platform.dto.UserEngagementDtos;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.EnterpriseTask;
import com.aiart.platform.entity.StylePackage;
import com.aiart.platform.entity.User;
import com.aiart.platform.entity.UserFavorite;
import com.aiart.platform.entity.UserNotification;
import com.aiart.platform.entity.UserSubscription;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.EnterpriseTaskMapper;
import com.aiart.platform.mapper.StylePackageMapper;
import com.aiart.platform.mapper.UserFavoriteMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.mapper.UserNotificationMapper;
import com.aiart.platform.mapper.UserSubscriptionMapper;
import com.aiart.platform.service.UserEngagementService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserEngagementServiceImpl implements UserEngagementService {
    private static final Set<String> TARGET_TYPES = Set.of("ARTWORK", "STYLE_PACKAGE", "TASK");
    private static final Set<String> SUBSCRIBABLE_TYPES = Set.of("STYLE_PACKAGE", "TASK");

    private final UserFavoriteMapper userFavoriteMapper;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserNotificationMapper userNotificationMapper;
    private final ArtworkMapper artworkMapper;
    private final StylePackageMapper stylePackageMapper;
    private final EnterpriseTaskMapper enterpriseTaskMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserEngagementDtos.ToggleResult toggleFavorite(Long userId, UserEngagementDtos.ToggleRequest request) {
        String targetType = normalizeTargetType(request.targetType());
        requireTargetExists(targetType, request.targetId());
        UserFavorite existing = userFavoriteMapper.selectOne(Wrappers.<UserFavorite>lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, request.targetId()));
        if (existing != null) {
            userFavoriteMapper.deleteById(existing.getId());
            return new UserEngagementDtos.ToggleResult(targetType, request.targetId(), false);
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(request.targetId());
        favorite.setCreatedAt(LocalDateTime.now());
        userFavoriteMapper.insert(favorite);
        return new UserEngagementDtos.ToggleResult(targetType, request.targetId(), true);
    }

    @Override
    @Transactional
    public UserEngagementDtos.ToggleResult toggleSubscription(Long userId, UserEngagementDtos.ToggleRequest request) {
        String targetType = normalizeTargetType(request.targetType());
        if (!SUBSCRIBABLE_TYPES.contains(targetType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "This target type does not support subscriptions");
        }
        requireTargetExists(targetType, request.targetId());
        UserSubscription existing = userSubscriptionMapper.selectOne(Wrappers.<UserSubscription>lambdaQuery()
                .eq(UserSubscription::getUserId, userId)
                .eq(UserSubscription::getTargetType, targetType)
                .eq(UserSubscription::getTargetId, request.targetId()));
        if (existing != null) {
            userSubscriptionMapper.deleteById(existing.getId());
            return new UserEngagementDtos.ToggleResult(targetType, request.targetId(), false);
        }
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setTargetType(targetType);
        subscription.setTargetId(request.targetId());
        subscription.setCreatedAt(LocalDateTime.now());
        userSubscriptionMapper.insert(subscription);
        return new UserEngagementDtos.ToggleResult(targetType, request.targetId(), true);
    }

    @Override
    public List<UserEngagementDtos.TargetSummaryView> favorites(Long userId, String targetType) {
        String normalizedType = normalizeOptionalTargetType(targetType, false);
        return userFavoriteMapper.selectList(Wrappers.<UserFavorite>lambdaQuery()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(StringUtils.hasText(normalizedType), UserFavorite::getTargetType, normalizedType)
                        .orderByDesc(UserFavorite::getCreatedAt))
                .stream()
                .map(item -> targetSummary(userId, item.getTargetType(), item.getTargetId(), item.getCreatedAt()))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<UserEngagementDtos.TargetSummaryView> subscriptions(Long userId, String targetType) {
        String normalizedType = normalizeOptionalTargetType(targetType, true);
        return userSubscriptionMapper.selectList(Wrappers.<UserSubscription>lambdaQuery()
                        .eq(UserSubscription::getUserId, userId)
                        .eq(StringUtils.hasText(normalizedType), UserSubscription::getTargetType, normalizedType)
                        .orderByDesc(UserSubscription::getCreatedAt))
                .stream()
                .map(item -> targetSummary(userId, item.getTargetType(), item.getTargetId(), item.getCreatedAt()))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public UserEngagementDtos.NotificationFeed notifications(Long userId, boolean unreadOnly, int page, int size) {
        Page<UserNotification> result = userNotificationMapper.selectPage(
                new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 100)),
                Wrappers.<UserNotification>lambdaQuery()
                        .eq(UserNotification::getUserId, userId)
                        .eq(unreadOnly, UserNotification::getRead, false)
                        .orderByDesc(UserNotification::getCreatedAt)
        );
        return new UserEngagementDtos.NotificationFeed(
                result.getRecords().stream().map(item -> notificationView(userId, item)).toList(),
                unreadCount(userId)
        );
    }

    @Override
    @Transactional
    public UserEngagementDtos.NotificationView markRead(Long userId, Long notificationId) {
        UserNotification notification = userNotificationMapper.selectOne(Wrappers.<UserNotification>lambdaQuery()
                .eq(UserNotification::getId, notificationId)
                .eq(UserNotification::getUserId, userId));
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Notification not found");
        }
        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            userNotificationMapper.updateById(notification);
        }
        return notificationView(userId, notification);
    }

    @Override
    @Transactional
    public long markAllRead(Long userId) {
        List<UserNotification> notifications = userNotificationMapper.selectList(Wrappers.<UserNotification>lambdaQuery()
                .eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getRead, false));
        if (notifications.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(item -> {
            item.setRead(true);
            item.setReadAt(now);
            userNotificationMapper.updateById(item);
        });
        return notifications.size();
    }

    @Override
    public long unreadCount(Long userId) {
        return userNotificationMapper.selectCount(Wrappers.<UserNotification>lambdaQuery()
                .eq(UserNotification::getUserId, userId)
                .eq(UserNotification::getRead, false));
    }

    @Override
    public Set<Long> subscriptionUserIds(String targetType, Long targetId) {
        String normalizedType = normalizeOptionalTargetType(targetType, true);
        if (!StringUtils.hasText(normalizedType)) {
            return Set.of();
        }
        return userSubscriptionMapper.selectList(Wrappers.<UserSubscription>lambdaQuery()
                        .eq(UserSubscription::getTargetType, normalizedType)
                        .eq(UserSubscription::getTargetId, targetId))
                .stream()
                .map(UserSubscription::getUserId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    @Override
    @Transactional
    public void notifyUsers(Collection<Long> userIds, Long actorUserId, String type, String title, String content,
                            String relatedType, Long relatedId) {
        Set<Long> targets = new LinkedHashSet<>();
        if (userIds != null) {
            for (Long userId : userIds) {
                if (userId == null) {
                    continue;
                }
                if (actorUserId != null && actorUserId.equals(userId)) {
                    continue;
                }
                targets.add(userId);
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        String normalizedRelatedType = normalizeOptionalTargetType(relatedType, false);
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : targets) {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setType(trimToNull(type));
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedType(normalizedRelatedType);
            notification.setRelatedId(relatedId);
            notification.setRead(false);
            notification.setCreatedAt(now);
            userNotificationMapper.insert(notification);
        }
    }

    private UserEngagementDtos.TargetSummaryView targetSummary(Long viewerId, String targetType, Long targetId,
                                                               LocalDateTime savedAt) {
        String normalizedType = normalizeTargetType(targetType);
        return switch (normalizedType) {
            case "ARTWORK" -> artworkSummary(viewerId, targetId, savedAt);
            case "STYLE_PACKAGE" -> stylePackageSummary(viewerId, targetId, savedAt);
            case "TASK" -> taskSummary(viewerId, targetId, savedAt);
            default -> null;
        };
    }

    private UserEngagementDtos.TargetSummaryView artworkSummary(Long viewerId, Long artworkId, LocalDateTime savedAt) {
        Artwork artwork = artworkMapper.selectById(artworkId);
        if (artwork == null) {
            return null;
        }
        User owner = userMapper.selectById(artwork.getUserId());
        String routeView = Objects.equals(artwork.getUserId(), viewerId) ? "library" : "community";
        return new UserEngagementDtos.TargetSummaryView(
                "ARTWORK",
                artwork.getId(),
                artwork.getTitle(),
                shortText(artwork.getPromptText(), 96),
                artwork.getImageUrl(),
                artwork.getStatus(),
                displayName(owner),
                routeView,
                "作品收藏",
                artwork.getCreatedAt(),
                savedAt
        );
    }

    private UserEngagementDtos.TargetSummaryView stylePackageSummary(Long viewerId, Long packageId, LocalDateTime savedAt) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null) {
            return null;
        }
        User owner = userMapper.selectById(stylePackage.getUserId());
        String routeView = Objects.equals(stylePackage.getUserId(), viewerId) ? "my-styles" : "style-market";
        String meta = normalizePoints(stylePackage.getPricePoints()) + " pts";
        return new UserEngagementDtos.TargetSummaryView(
                "STYLE_PACKAGE",
                stylePackage.getId(),
                stylePackage.getName(),
                shortText(firstNonBlank(stylePackage.getStyleStatement(), stylePackage.getDescription()), 120),
                firstNonBlank(stylePackage.getCoverImageUrl(), featuredArtworkImage(stylePackage.getFeaturedArtworkId())),
                stylePackage.getStatus(),
                displayName(owner),
                routeView,
                meta,
                stylePackage.getUpdatedAt(),
                savedAt
        );
    }

    private UserEngagementDtos.TargetSummaryView taskSummary(Long viewerId, Long taskId, LocalDateTime savedAt) {
        EnterpriseTask task = enterpriseTaskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        User owner = userMapper.selectById(task.getPublisherId());
        String routeView = Objects.equals(task.getPublisherId(), viewerId) ? "my-tasks" : "task-market";
        String meta = normalizePoints(task.getBudgetPoints()) + " pts";
        return new UserEngagementDtos.TargetSummaryView(
                "TASK",
                task.getId(),
                task.getTitle(),
                shortText(firstNonBlank(task.getDescription(), task.getRequirementsJson()), 120),
                null,
                task.getStatus(),
                displayName(owner),
                routeView,
                meta,
                task.getUpdatedAt(),
                savedAt
        );
    }

    private UserEngagementDtos.NotificationView notificationView(Long viewerId, UserNotification item) {
        return new UserEngagementDtos.NotificationView(
                item.getId(),
                item.getType(),
                item.getTitle(),
                item.getContent(),
                item.getRelatedType(),
                item.getRelatedId(),
                routeView(viewerId, item.getRelatedType(), item.getRelatedId()),
                Boolean.TRUE.equals(item.getRead()),
                item.getCreatedAt(),
                item.getReadAt()
        );
    }

    private String routeView(Long viewerId, String relatedType, Long relatedId) {
        if (!StringUtils.hasText(relatedType) || relatedId == null) {
            return "account";
        }
        return switch (normalizeTargetType(relatedType)) {
            case "ARTWORK" -> {
                Artwork artwork = artworkMapper.selectById(relatedId);
                yield artwork != null && Objects.equals(artwork.getUserId(), viewerId) ? "library" : "community";
            }
            case "STYLE_PACKAGE" -> {
                StylePackage stylePackage = stylePackageMapper.selectById(relatedId);
                yield stylePackage != null && Objects.equals(stylePackage.getUserId(), viewerId) ? "my-styles" : "style-market";
            }
            case "TASK" -> {
                EnterpriseTask task = enterpriseTaskMapper.selectById(relatedId);
                yield task != null && Objects.equals(task.getPublisherId(), viewerId) ? "my-tasks" : "task-market";
            }
            default -> "account";
        };
    }

    private void requireTargetExists(String targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case "ARTWORK" -> artworkMapper.selectById(targetId) != null;
            case "STYLE_PACKAGE" -> stylePackageMapper.selectById(targetId) != null;
            case "TASK" -> enterpriseTaskMapper.selectById(targetId) != null;
            default -> false;
        };
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Target not found");
        }
    }

    private String normalizeTargetType(String value) {
        String normalized = normalizeOptionalTargetType(value, false);
        if (!StringUtils.hasText(normalized) || !TARGET_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported target type");
        }
        return normalized;
    }

    private String normalizeOptionalTargetType(String value, boolean subscribableOnly) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!TARGET_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported target type");
        }
        if (subscribableOnly && !SUBSCRIBABLE_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported target type");
        }
        return normalized;
    }

    private String displayName(User user) {
        if (user == null) {
            return "未知用户";
        }
        return StringUtils.hasText(user.getDisplayName()) ? user.getDisplayName() : user.getUsername();
    }

    private String shortText(String value, int maxLength) {
        String source = trimToNull(value);
        if (source == null) {
            return "";
        }
        if (source.length() <= maxLength) {
            return source;
        }
        return source.substring(0, Math.max(0, maxLength - 1)) + "...";
    }

    private String featuredArtworkImage(Long artworkId) {
        if (artworkId == null) {
            return null;
        }
        Artwork artwork = artworkMapper.selectById(artworkId);
        return artwork == null ? null : artwork.getImageUrl();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizePoints(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).stripTrailingZeros().toPlainString();
    }
}
