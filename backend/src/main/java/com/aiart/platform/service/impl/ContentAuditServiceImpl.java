package com.aiart.platform.service.impl;

import com.aiart.platform.dto.AuditDtos;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.ContentAudit;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.ContentAuditMapper;
import com.aiart.platform.service.ContentAuditService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentAuditServiceImpl implements ContentAuditService {
    private final ContentAuditMapper contentAuditMapper;
    private final ArtworkMapper artworkMapper;

    @Override
    public List<AuditDtos.AuditView> pending(int page, int size) {
        Page<ContentAudit> result = contentAuditMapper.selectPage(page(page, size), Wrappers.<ContentAudit>lambdaQuery()
                .eq(ContentAudit::getStatus, "PENDING")
                .orderByAsc(ContentAudit::getCreatedAt));
        return result.getRecords().stream().map(this::view).toList();
    }

    @Override
    public List<AuditDtos.AuditView> myAudits(Long userId, int page, int size) {
        Page<ContentAudit> result = contentAuditMapper.selectPage(page(page, size), Wrappers.<ContentAudit>lambdaQuery()
                .eq(ContentAudit::getSubmitterId, userId)
                .orderByDesc(ContentAudit::getCreatedAt));
        return result.getRecords().stream().map(this::view).toList();
    }

    @Override
    @Transactional
    public AuditDtos.AuditView review(Long auditorId, Long auditId, AuditDtos.ReviewRequest request) {
        ContentAudit audit = contentAuditMapper.selectById(auditId);
        if (audit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Audit not found");
        }
        if (!"PENDING".equals(audit.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Audit has already been reviewed");
        }
        String status = request.status().trim().toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Review status must be APPROVED or REJECTED");
        }
        audit.setStatus(status);
        audit.setAuditorId(auditorId);
        audit.setCommentText(request.comment());
        audit.setAuditedAt(LocalDateTime.now());
        if ("ARTWORK".equals(audit.getContentType())) {
            Artwork artwork = artworkMapper.selectById(audit.getContentId());
            if (artwork != null) {
                artwork.setVisibility("APPROVED".equals(status) ? "PUBLIC" : "PRIVATE");
                artwork.setStatus("ACTIVE");
                artworkMapper.updateById(artwork);
            }
        }
        contentAuditMapper.updateById(audit);
        return view(audit);
    }

    private Page<ContentAudit> page(int page, int size) {
        return new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 30));
    }

    private AuditDtos.AuditView view(ContentAudit audit) {
        Artwork artwork = "ARTWORK".equals(audit.getContentType()) ? artworkMapper.selectById(audit.getContentId()) : null;
        return new AuditDtos.AuditView(
                audit.getId(),
                audit.getContentType(),
                audit.getContentId(),
                audit.getSubmitterId(),
                audit.getAuditorId(),
                audit.getStatus(),
                audit.getCommentText(),
                artwork == null ? null : artwork.getTitle(),
                artwork == null ? null : artwork.getImageUrl(),
                audit.getCreatedAt(),
                audit.getAuditedAt());
    }
}
