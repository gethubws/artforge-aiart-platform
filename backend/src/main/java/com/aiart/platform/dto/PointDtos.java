package com.aiart.platform.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PointDtos {
    private PointDtos() {
    }

    public record AccountView(BigDecimal balance, BigDecimal frozenBalance, List<TransactionView> transactions) {
    }

    public record TransactionView(Long id, BigDecimal amount, String direction, String reason,
                                  String referenceType, Long referenceId, LocalDateTime createdAt) {
    }

    public record ClaimRequest(@DecimalMin("1.0") BigDecimal amount) {
    }
}
