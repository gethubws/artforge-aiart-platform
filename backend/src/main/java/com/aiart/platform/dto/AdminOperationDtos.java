package com.aiart.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminOperationDtos {
    private AdminOperationDtos() {
    }

    public record OperationPage(List<OperationRow> items,
                                long page,
                                long size,
                                long total,
                                long pages) {
    }

    public record OperationRow(Long id,
                               Long operatorId,
                               String operatorName,
                               String action,
                               String targetType,
                               Long targetId,
                               String requestMethod,
                               String requestPath,
                               String summary,
                               String ipAddress,
                               LocalDateTime createdAt) {
    }
}
