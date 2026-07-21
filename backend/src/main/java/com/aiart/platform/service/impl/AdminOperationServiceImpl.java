package com.aiart.platform.service.impl;

import com.aiart.platform.dto.AdminOperationDtos;
import com.aiart.platform.entity.AdminOperationLog;
import com.aiart.platform.entity.User;
import com.aiart.platform.mapper.AdminOperationLogMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.service.AdminOperationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOperationServiceImpl implements AdminOperationService {
    private final AdminOperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    @Override
    public void record(Long operatorId,
                       String action,
                       String targetType,
                       Long targetId,
                       String requestMethod,
                       String requestPath,
                       String summary,
                       String ipAddress) {
        AdminOperationLog operation = new AdminOperationLog();
        operation.setOperatorId(operatorId);
        operation.setAction(action);
        operation.setTargetType(targetType);
        operation.setTargetId(targetId);
        operation.setRequestMethod(requestMethod);
        operation.setRequestPath(requestPath);
        operation.setSummary(summary);
        operation.setIpAddress(ipAddress);
        operation.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(operation);
    }

    @Override
    public AdminOperationDtos.OperationPage operations(int page, int size, String action, String targetType) {
        long current = Math.max(1, page);
        long limit = Math.min(Math.max(1, size), 50);
        var query = Wrappers.<AdminOperationLog>lambdaQuery();
        if (StringUtils.hasText(action)) {
            query.eq(AdminOperationLog::getAction, action.trim().toUpperCase());
        }
        if (StringUtils.hasText(targetType)) {
            query.eq(AdminOperationLog::getTargetType, targetType.trim().toUpperCase());
        }
        Page<AdminOperationLog> result = operationLogMapper.selectPage(
                new Page<>(current, limit), query.orderByDesc(AdminOperationLog::getCreatedAt));
        var operatorIds = result.getRecords().stream()
                .map(AdminOperationLog::getOperatorId)
                .distinct()
                .toList();
        Map<Long, User> users = operatorIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        var rows = result.getRecords().stream()
                .map(operation -> row(operation, users.get(operation.getOperatorId())))
                .toList();
        return new AdminOperationDtos.OperationPage(
                rows, result.getCurrent(), result.getSize(), result.getTotal(), result.getPages());
    }

    private AdminOperationDtos.OperationRow row(AdminOperationLog operation, User operator) {
        String operatorName = operator == null ? "Unknown administrator" : operator.getDisplayName();
        return new AdminOperationDtos.OperationRow(
                operation.getId(), operation.getOperatorId(), operatorName, operation.getAction(),
                operation.getTargetType(), operation.getTargetId(), operation.getRequestMethod(),
                operation.getRequestPath(), operation.getSummary(), operation.getIpAddress(), operation.getCreatedAt());
    }
}
