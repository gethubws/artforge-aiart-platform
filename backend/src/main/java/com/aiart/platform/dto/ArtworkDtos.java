package com.aiart.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public final class ArtworkDtos {
    private ArtworkDtos() {
    }

    public record TagSummary(Long id, String name, String displayNameZh, String categoryName) {
    }

    public record ArtworkCard(Long id, String title, String imageUrl, String promptText, String visibility, String status, LocalDateTime createdAt,
                              List<TagSummary> tags) {
    }

    public record ArtworkPage(List<ArtworkCard> items, long page, long size, long total, boolean hasNext) {
    }

    public record ArtworkDetail(Long id, String title, String imageUrl, String promptText, String negativePrompt,
                                String generationParamsJson, String visibility, String status, LocalDateTime createdAt,
                                List<TagSummary> tags) {
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 120) String title,
            String promptText,
            String negativePrompt,
            String visibility,
            List<Long> tagIds) {
    }

    public record BulkRequest(
            @NotEmpty @Size(max = 100) List<Long> artworkIds) {
    }

    public record BulkVisibilityRequest(
            @NotEmpty @Size(max = 100) List<Long> artworkIds,
            @NotBlank String visibility) {
    }
}
