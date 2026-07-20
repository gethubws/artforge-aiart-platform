package com.aiart.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class StylePackageDtos {
    private StylePackageDtos() {
    }

    public record SaveRequest(
            @NotBlank @Size(max = 120) String name,
            String description,
            String coverImageUrl,
            String styleStatement,
            String promptGuide,
            String negativePromptGuide,
            Long featuredArtworkId,
            @DecimalMin("0.0") BigDecimal pricePoints,
            List<Long> tagIds,
            List<String> tagNames,
            List<CollaboratorInput> collaborators) {
    }

    public record ListQuery(
            String keyword,
            Long tagId,
            String status,
            String sort) {
    }

    public record CollaboratorInput(
            @NotNull Long userId,
            @Size(max = 40) String role) {
    }

    public record SubmitRequest(@NotNull Long artworkId, String note) {
    }

    public record ReviewRequest(@NotBlank String status, String comment) {
    }

    public record RatingRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 1000) String comment) {
    }

    public record TagSummary(Long id, String name, String displayNameZh, String previewImageUrl) {
    }

    public record ArtworkSummary(Long id, String title, String imageUrl, String visibility, String status) {
    }

    public record CollaboratorSummary(Long userId, String displayName, String avatarUrl, String role) {
    }

    public record Stats(long accessCount, long submissionCount, long approvedArtworkCount, long versionCount,
                        long reviewCount, long collaboratorCount, double averageRating) {
    }

    public record Card(Long id, String name, String description, String coverImageUrl,
                       String styleStatement, String promptGuide, String negativePromptGuide,
                       Long featuredArtworkId, BigDecimal pricePoints, String status, Long userId,
                       boolean owner, boolean accessible, Stats stats,
                       List<TagSummary> tags, List<ArtworkSummary> artworks, List<CollaboratorSummary> collaborators,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record Detail(Long id, String name, String description, String coverImageUrl,
                         String styleStatement, String promptGuide, String negativePromptGuide,
                         Long featuredArtworkId, BigDecimal pricePoints, String status, Long userId,
                         boolean owner, boolean accessible, Stats stats,
                         List<TagSummary> tags, List<ArtworkSummary> artworks, List<CollaboratorSummary> collaborators,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record VersionView(Long id, Long stylePackageId, Integer versionNumber, String name, String description,
                              String coverImageUrl, String styleStatement, String promptGuide,
                              String negativePromptGuide, Long featuredArtworkId, Integer artworkCount,
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
