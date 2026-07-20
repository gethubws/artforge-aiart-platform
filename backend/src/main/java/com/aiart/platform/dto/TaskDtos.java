package com.aiart.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class TaskDtos {
    private TaskDtos() {
    }

    public record SaveRequest(@NotBlank @Size(max = 140) String title,
                              String description,
                              String requirementsText,
                              @DecimalMin("0.0") BigDecimal budgetPoints,
                              LocalDateTime deadline) {
    }

    public record SubmitRequest(@NotNull Long artworkId, String note) {
    }

    public record ReviewRequest(@NotBlank String status, String comment,
                                @DecimalMin("0.0") BigDecimal rewardPoints) {
    }

    public record ListQuery(String keyword, String status, String tier, String sort) {
    }

    public record TaskCard(Long id, Long publisherId, String title, String description, String requirementsText,
                           BigDecimal budgetPoints, String status, LocalDateTime deadline,
                           long submissionCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record TaskPage(List<TaskCard> items, int page, int size, long total, boolean hasNext) {
    }

    public record SubmissionView(Long id, Long taskId, Long submitterId, Long artworkId, String artworkTitle,
                                 String artworkImageUrl, String note, String status, BigDecimal rewardPoints,
                                 String reviewComment, LocalDateTime createdAt, LocalDateTime reviewedAt) {
    }
}
