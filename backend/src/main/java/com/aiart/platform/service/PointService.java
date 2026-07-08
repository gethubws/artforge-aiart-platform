package com.aiart.platform.service;

import com.aiart.platform.dto.PointDtos;

import java.math.BigDecimal;

public interface PointService {
    PointDtos.AccountView account(Long userId, int page, int size);

    PointDtos.AccountView claimDaily(Long userId);

    void spend(Long userId, BigDecimal amount, String reason, String referenceType, Long referenceId);

    void earn(Long userId, BigDecimal amount, String reason, String referenceType, Long referenceId);
}
