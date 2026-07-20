package com.aiart.platform.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TagDtos {
    private TagDtos() {
    }

    public record TagNode(Long id, String name, String displayNameZh, String descriptionZh,
                          String promptText, String negativePromptText, String previewImageUrl,
                          BigDecimal weight, String visibility) {
    }

    public record TagCategoryNode(Long id, String name, String slug, List<TagNode> tags) {
    }

    public record PromptBuildRequest(List<Long> tagIds, String freeText, String negativeText) {
    }

    public record PromptBuildResponse(String prompt, String negativePrompt, List<Long> tagIds) {
    }

    public record TagSaveRequest(
            @NotNull Long categoryId,
            @NotBlank @Size(max = 80) String name,
            @Size(max = 80) String displayNameZh,
            @Size(max = 255) String descriptionZh,
            @NotBlank @Size(max = 255) String promptText,
            @Size(max = 255) String negativePromptText,
            @Size(max = 512) String previewImageUrl,
            @DecimalMin("0.1") BigDecimal weight,
            String visibility) {
    }

    public record CategorySaveRequest(@NotBlank @Size(max = 80) String name,
                                      @NotBlank @Size(max = 80) String slug,
                                      Integer sortOrder) {
    }
}
