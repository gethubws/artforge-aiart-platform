package com.aiart.platform.service;

import com.aiart.platform.dto.AuditDtos;

import java.util.List;

public interface ContentAuditService {
    List<AuditDtos.AuditView> pending(int page, int size);

    List<AuditDtos.AuditView> myAudits(Long userId, int page, int size);

    AuditDtos.AuditView review(Long auditorId, Long auditId, AuditDtos.ReviewRequest request);
}
