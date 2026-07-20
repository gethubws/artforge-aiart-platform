package com.aiart.platform.service.impl;

import com.aiart.platform.dto.AdminUserDtos;
import com.aiart.platform.entity.User;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.AiGenerationJobMapper;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.EnterpriseTaskMapper;
import com.aiart.platform.mapper.StylePackageMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.service.AdminUserService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private static final Set<String> ROLES = Set.of("USER", "ADMIN");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "SUSPENDED");

    private final UserMapper userMapper;
    private final ArtworkMapper artworkMapper;
    private final AiGenerationJobMapper generationJobMapper;
    private final StylePackageMapper stylePackageMapper;
    private final EnterpriseTaskMapper enterpriseTaskMapper;

    @Override
    public AdminUserDtos.UserPage users(int page, int size, String keyword, String role, String status) {
        long current = Math.max(1, page);
        long limit = Math.min(Math.max(1, size), 50);
        var query = Wrappers.<User>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            query.and(wrapper -> wrapper.like(User::getUsername, value).or().like(User::getDisplayName, value));
        }
        if (StringUtils.hasText(role)) {
            query.eq(User::getRole, normalize(role));
        }
        if (StringUtils.hasText(status)) {
            query.eq(User::getStatus, normalize(status));
        }
        Page<User> result = userMapper.selectPage(new Page<>(current, limit),
                query.orderByDesc(User::getCreatedAt));
        List<Long> userIds = result.getRecords().stream().map(User::getId).toList();
        Map<Long, Long> artworkCounts = groupedCounts(artworkMapper, "user_id", userIds);
        Map<Long, Long> generationCounts = groupedCounts(generationJobMapper, "user_id", userIds);
        Map<Long, Long> styleCounts = groupedCounts(stylePackageMapper, "user_id", userIds);
        Map<Long, Long> taskCounts = groupedCounts(enterpriseTaskMapper, "publisher_id", userIds);
        List<AdminUserDtos.UserRow> rows = result.getRecords().stream()
                .map(user -> row(user, artworkCounts, generationCounts, styleCounts, taskCounts))
                .toList();
        return new AdminUserDtos.UserPage(rows, result.getCurrent(), result.getSize(), result.getTotal(), result.getPages());
    }

    @Override
    @Transactional
    public AdminUserDtos.UserRow updateUser(Long operatorId, Long userId, AdminUserDtos.UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        String nextRole = StringUtils.hasText(request.role()) ? normalize(request.role()) : user.getRole();
        String nextStatus = StringUtils.hasText(request.status()) ? normalize(request.status()) : user.getStatus();
        if (!ROLES.contains(nextRole)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported user role");
        }
        if (!STATUSES.contains(nextStatus)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported user status");
        }
        if (userId.equals(operatorId) && (!"ADMIN".equals(nextRole) || !"ACTIVE".equals(nextStatus))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "You cannot remove your own active admin access");
        }
        boolean removesActiveAdmin = "ADMIN".equals(user.getRole()) && "ACTIVE".equals(user.getStatus())
                && (!"ADMIN".equals(nextRole) || !"ACTIVE".equals(nextStatus));
        if (removesActiveAdmin) {
            long activeAdmins = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                    .eq(User::getRole, "ADMIN")
                    .eq(User::getStatus, "ACTIVE"));
            if (activeAdmins <= 1) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "At least one active administrator is required");
            }
        }
        if (request.displayName() != null) {
            String displayName = request.displayName().trim();
            if (displayName.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Display name is required");
            }
            user.setDisplayName(displayName);
        }
        user.setRole(nextRole);
        user.setStatus(nextStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return row(user, Map.of(), Map.of(), Map.of(), Map.of());
    }

    private AdminUserDtos.UserRow row(User user,
                                      Map<Long, Long> artworkCounts,
                                      Map<Long, Long> generationCounts,
                                      Map<Long, Long> styleCounts,
                                      Map<Long, Long> taskCounts) {
        return new AdminUserDtos.UserRow(
                user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl(), user.getRole(), user.getStatus(),
                user.getPointBalance(), artworkCounts.getOrDefault(user.getId(), 0L), generationCounts.getOrDefault(user.getId(), 0L),
                styleCounts.getOrDefault(user.getId(), 0L), taskCounts.getOrDefault(user.getId(), 0L), user.getCreatedAt(), user.getUpdatedAt());
    }

    private <T> Map<Long, Long> groupedCounts(BaseMapper<T> mapper,
                                               String ownerColumn,
                                               List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<T> query = new QueryWrapper<>();
        query.select(ownerColumn + " AS owner_id", "COUNT(*) AS item_count")
                .in(ownerColumn, userIds)
                .groupBy(ownerColumn);
        List<Map<String, Object>> rows = mapper.selectMaps(query);
        return rows.stream().collect(Collectors.toMap(
                row -> number(row.get("owner_id")),
                row -> number(row.get("item_count"))));
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }
}
