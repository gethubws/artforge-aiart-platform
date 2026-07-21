package com.aiart.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

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
            @Size(max = 64) String licenseType,
            @Size(max = 500) String licenseSummary,
            Boolean commercialUse,
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

    public record AssetSaveRequest(
            @Size(max = 80) String logicalKey,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 64) String categoryKey,
            @Size(max = 40) String assetType,
            String description,
            @NotBlank @Size(max = 512) String previewImageUrl,
            @Size(max = 512) String fileUrl,
            @Size(max = 512) String thumbnailUrl,
            String promptText,
            String negativePromptText,
            String generationParamsJson,
            @Min(1) Integer width,
            @Min(1) Integer height,
            @Size(max = 20) String fileFormat,
            @Size(max = 32) String backgroundMode,
            @Size(max = 64) String licenseScope,
            Integer sortOrder) {
    }

    public record AssetBatchRequest(
            @NotNull @Size(min = 1, max = 100) List<@Valid AssetSaveRequest> assets) {
    }

    public record AssetUploadView(String fileUrl, String originalFilename, String contentType, long size) {
    }

    public record AssetDownload(String fileUrl, String filename, String contentType) {
    }

    public record AssetPreview(Long id, String logicalKey, String name, String categoryKey,
                               String previewImageUrl, String thumbnailUrl, Integer revisionNumber) {
    }

    public record AssetView(Long id, Long stylePackageId, Long contributorId, String logicalKey,
                            Integer revisionNumber, String name, String categoryKey, String assetType,
                            String description, String previewImageUrl, String fileUrl, String thumbnailUrl,
                            String promptText, String negativePromptText, String generationParamsJson,
                            Integer width, Integer height, String fileFormat, String backgroundMode,
                            String licenseScope, String status, Integer sortOrder, boolean downloadable,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record AssetManifest(Long stylePackageId, Long versionId, Integer versionNumber,
                                String packageName, String licenseType, String licenseSummary,
                                boolean commercialUse, Integer resourceCount, Integer categoryCount,
                                List<AssetView> assets) {
    }

    public record Stats(long accessCount, long submissionCount, long approvedArtworkCount, long versionCount,
                        long reviewCount, long collaboratorCount, long resourceCount,
                        long categoryCount, double averageRating) {
    }

    public record Card(Long id, String name, String description, String coverImageUrl,
                       String styleStatement, String promptGuide, String negativePromptGuide,
                       String licenseType, String licenseSummary, boolean commercialUse,
                       Long featuredArtworkId, BigDecimal pricePoints, String status, Long userId,
                       boolean owner, boolean editable, boolean accessible, Stats stats,
                       List<TagSummary> tags, List<AssetPreview> assetPreviews,
                       List<ArtworkSummary> artworks, List<CollaboratorSummary> collaborators,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record PackagePage(List<Card> items, int page, int size, long total, boolean hasNext) {
    }

    public record Detail(Long id, String name, String description, String coverImageUrl,
                         String styleStatement, String promptGuide, String negativePromptGuide,
                         String licenseType, String licenseSummary, boolean commercialUse,
                         Long featuredArtworkId, BigDecimal pricePoints, String status, Long userId,
                         boolean owner, boolean editable, boolean accessible, Stats stats,
                         List<TagSummary> tags, List<AssetPreview> assetPreviews,
                         List<ArtworkSummary> artworks, List<CollaboratorSummary> collaborators,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record VersionView(Long id, Long stylePackageId, Integer versionNumber, String name, String description,
                              String coverImageUrl, String styleStatement, String promptGuide,
                              String negativePromptGuide, String licenseType, String licenseSummary,
                              boolean commercialUse, Long featuredArtworkId, Integer artworkCount,
                              Integer resourceCount, Integer categoryCount,
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
