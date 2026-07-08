package com.aiart.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class StylePackageDtos {
    private StylePackageDtos() {
    }

    public record SaveRequest(
            @NotBlank @Size(max = 120) String name,
            String description,
            String coverImageUrl,
            @NotBlank String promptTemplate,
            String negativePromptTemplate,
            @DecimalMin("0.0") BigDecimal pricePoints) {
    }

    public record SubmitRequest(@NotNull Long artworkId, String note) {
    }

    public record ReviewRequest(@NotBlank String status, String comment) {
    }

    public record RatingRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 1000) String comment) {
    }

    public record Stats(long accessCount, long submissionCount, long approvedArtworkCount, long versionCount,
                        long reviewCount, double averageRating) {
    }

    public record Card(Long id, String name, String description, String coverImageUrl,
                       String promptTemplate, String negativePromptTemplate, BigDecimal pricePoints,
                       String status, Long userId, boolean owner, boolean accessible, Stats stats,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record Detail(Long id, String name, String description, String coverImageUrl,
                         String promptTemplate, String negativePromptTemplate, BigDecimal pricePoints,
                         String status, Long userId, boolean owner, boolean accessible, Stats stats,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record VersionView(Long id, Long stylePackageId, Integer versionNumber, String name, String description,
                              String coverImageUrl, String promptTemplate, String negativePromptTemplate,
                              BigDecimal pricePoints, String changeNote, LocalDateTime createdAt) {
    }

    public record RatingView(Long id, Long stylePackageId, Long reviewerId, Integer rating, String comment,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record SubmissionView(Long id, Long stylePackageId, Long submitterId, Long artworkId,
                                 String artworkTitle, String artworkImageUrl, String note, String status,
                                 String reviewComment, LocalDateTime createdAt, LocalDateTime reviewedAt) {
    }
}
