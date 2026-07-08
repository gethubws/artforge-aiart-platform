package com.aiart.platform.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public final class AuditDtos {
    private AuditDtos() {
    }

    public record ReviewRequest(@NotBlank String status, String comment) {
    }

    public record AuditView(Long id, String contentType, Long contentId, Long submitterId, Long auditorId,
                            String status, String commentText, String artworkTitle, String artworkImageUrl,
                            LocalDateTime createdAt, LocalDateTime auditedAt) {
    }
}
