package com.aiart.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

public final class GenerationDtos {
    private GenerationDtos() {
    }

    public record LoraSelection(
            @NotBlank String name,
            @DecimalMin("0.0") @DecimalMax("2.0") Double weight) {
    }

    public record GenerateRequest(
            @NotBlank String prompt,
            String negativePrompt,
            String freeText,
            String negativeText,
            String title,
            List<Long> tagIds,
            String model,
            String vae,
            List<LoraSelection> loras,
            String samplerName,
            @Min(256) @Max(2048) Integer width,
            @Min(256) @Max(2048) Integer height,
            @Min(1) @Max(150) Integer steps,
            @DecimalMin("1.0") @DecimalMax("30.0") Double cfgScale,
            Long seed,
            Boolean restoreFaces,
            Boolean tiling,
            @Min(1) @Max(4) Integer batchSize,
            @Min(1) @Max(4) Integer batchCount,
            @Min(1) @Max(12) Integer clipSkip,
            Boolean enableHr,
            Double denoisingStrength,
            Double hrScale,
            String hrUpscaler,
            @Min(0) @Max(150) Integer hrSecondPassSteps,
            @Size(max = 4) List<String> initImages,
            @Min(0) @Max(3) Integer resizeMode,
            Map<String, Object> overrideSettings,
            Map<String, Object> extraPayload) {
    }

    public record GeneratedArtwork(Long artworkId, String imageUrl, String title) {
    }

    public record GenerateResponse(Long jobId, Long artworkId, String imageUrl, String status, String title,
                                   List<GeneratedArtwork> artworks) {
    }

    public record QueuedGenerationResponse(Long jobId, String status, String mode) {
    }

    public record GenerationJobView(Long id, String provider, String status, String title, String imageUrl,
                                    String promptText, String negativePrompt, String paramsJson, String errorMessage,
                                    List<GeneratedArtwork> artworks, LocalDateTime createdAt, LocalDateTime completedAt) {
    }

    public record SdTxt2ImgResponse(List<String> images, Map<String, Object> parameters, String info) {
    }
}
