package com.aiart.platform.service;

import com.aiart.platform.dto.AdminOperationDtos;

public interface AdminOperationService {
    void record(Long operatorId,
                String action,
                String targetType,
                Long targetId,
                String requestMethod,
                String requestPath,
                String summary,
                String ipAddress);

    AdminOperationDtos.OperationPage operations(int page, int size, String action, String targetType);
}
