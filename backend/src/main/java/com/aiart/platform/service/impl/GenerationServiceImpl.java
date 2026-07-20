package com.aiart.platform.service.impl;

import com.aiart.platform.dto.GenerationDtos;
import com.aiart.platform.entity.AiGenerationJob;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.ArtworkTag;
import com.aiart.platform.entity.Tag;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.AiGenerationJobMapper;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.ArtworkTagMapper;
import com.aiart.platform.mapper.TagMapper;
import com.aiart.platform.service.FileStorageService;
import com.aiart.platform.service.GenerationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PreDestroy;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import java.time.Duration;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

@Service
@RequiredArgsConstructor
public class GenerationServiceImpl implements GenerationService {
    private final AiGenerationJobMapper jobMapper;
    private final ArtworkMapper artworkMapper;
    private final ArtworkTagMapper artworkTagMapper;
    private final TagMapper tagMapper;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final ExecutorService generationExecutor = Executors.newSingleThreadExecutor(new GenerationThreadFactory());
    private final ConcurrentMap<Long, Future<?>> activeTasks = new ConcurrentHashMap<>();

    @Value("${aiart.generation.sd-webui.base-url:http://127.0.0.1:7860}")
    private String sdBaseUrl;

    @Value("${aiart.generation.sd-webui.timeout-seconds:300}")
    private long timeoutSeconds;

    @Override
    @Transactional
    public GenerationDtos.GenerateResponse generate(Long userId, GenerationDtos.GenerateRequest request) {
        return generateInternal(userId, request, false);
    }

    @Override
    public GenerationDtos.QueuedGenerationResponse enqueueTxt2Img(Long userId, GenerationDtos.GenerateRequest request) {
        return enqueue(userId, request, false);
    }

    @Override
    public GenerationDtos.QueuedGenerationResponse enqueueImg2Img(Long userId, GenerationDtos.GenerateRequest request) {
        if (request.initImages() == null || request.initImages().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Init image is required for image-to-image generation");
        }
        return enqueue(userId, request, true);
    }

    @Override
    @Transactional
    public GenerationDtos.GenerateResponse generateImg2Img(Long userId, GenerationDtos.GenerateRequest request) {
        if (request.initImages() == null || request.initImages().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Init image is required for image-to-image generation");
        }
        return generateInternal(userId, request, true);
    }

    private GenerationDtos.GenerateResponse generateInternal(Long userId, GenerationDtos.GenerateRequest request, boolean imageToImage) {
        AiGenerationJob job = createJob(userId, request, imageToImage, "RUNNING");
        return runGeneration(job, request, imageToImage);
    }

    private GenerationDtos.QueuedGenerationResponse enqueue(Long userId, GenerationDtos.GenerateRequest request, boolean imageToImage) {
        AiGenerationJob job = createJob(userId, request, imageToImage, "QUEUED");
        Future<?> future = generationExecutor.submit(() -> runQueuedJob(job.getId(), userId, request, imageToImage));
        activeTasks.put(job.getId(), future);
        return new GenerationDtos.QueuedGenerationResponse(job.getId(), job.getStatus(), imageToImage ? "img2img" : "txt2img");
    }

    private AiGenerationJob createJob(Long userId, GenerationDtos.GenerateRequest request, boolean imageToImage, String status) {
        LocalDateTime now = LocalDateTime.now();
        AiGenerationJob job = new AiGenerationJob();
        job.setUserId(userId);
        job.setProvider(imageToImage ? "SD_WEBUI_IMG2IMG" : "SD_WEBUI_TXT2IMG");
        job.setPromptText(composePromptWithLoras(request));
        job.setNegativePrompt(request.negativePrompt());
        job.setParamsJson(toJson(safeRequestForStorage(request)));
        job.setStatus(status);
        job.setCreatedAt(now);
        jobMapper.insert(job);
        return job;
    }

    private void runQueuedJob(Long jobId, Long userId, GenerationDtos.GenerateRequest request, boolean imageToImage) {
        AiGenerationJob job = jobMapper.selectById(jobId);
        if (job == null || !job.getUserId().equals(userId)) {
            return;
        }
        if ("CANCELLED".equals(job.getStatus())) {
            activeTasks.remove(jobId);
            return;
        }
        job.setStatus("RUNNING");
        jobMapper.updateById(job);
        try {
            runGeneration(job, request, imageToImage);
        } catch (RuntimeException ignored) {
            // runGeneration has already persisted the job failure message.
        } finally {
            activeTasks.remove(jobId);
        }
    }

    private GenerationDtos.GenerateResponse runGeneration(AiGenerationJob job, GenerationDtos.GenerateRequest request, boolean imageToImage) {
        try {
            GenerationDtos.SdTxt2ImgResponse response = callGeneration(request, imageToImage);
            List<String> images = response == null ? List.of() : response.images();
            if (images == null || images.isEmpty()) {
                throw new BusinessException(ErrorCode.SD_UNAVAILABLE, "Generation provider returned no image");
            }
            if (jobWasCancelled(job.getId())) {
                job.setStatus("CANCELLED");
                job.setCompletedAt(LocalDateTime.now());
                jobMapper.updateById(job);
                return new GenerationDtos.GenerateResponse(job.getId(), null, null, job.getStatus(), titleFromParams(job.getParamsJson()), List.of());
            }

            List<Long> selectedTagIds = selectedPublicTagIds(request);
            List<GenerationDtos.GeneratedArtwork> generatedArtworks = new ArrayList<>();
            for (int index = 0; index < images.size(); index++) {
                String imageUrl = fileStorageService.saveBase64Png(images.get(index));
                Artwork artwork = new Artwork();
                artwork.setUserId(job.getUserId());
                artwork.setTitle(buildArtworkTitle(request.title(), index, images.size()));
                artwork.setPromptText(composePromptWithLoras(request));
                artwork.setNegativePrompt(request.negativePrompt());
                artwork.setImageUrl(imageUrl);
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("request", safeRequestForStorage(request));
                metadata.put("tagIds", selectedTagIds);
                metadata.put("imageIndex", index + 1);
                metadata.put("providerInfo", response == null ? null : response.info());
                artwork.setGenerationParamsJson(toJson(metadata));
                artwork.setSourceJobId(job.getId());
                artwork.setVisibility("PRIVATE");
                artwork.setStatus("ACTIVE");
                artwork.setCreatedAt(LocalDateTime.now());
                artworkMapper.insert(artwork);
                saveArtworkTags(artwork, selectedTagIds);
                generatedArtworks.add(new GenerationDtos.GeneratedArtwork(artwork.getId(), imageUrl, artwork.getTitle()));
            }
            GenerationDtos.GeneratedArtwork firstArtwork = generatedArtworks.get(0);

            job.setStatus("COMPLETED");
            job.setImageUrl(firstArtwork.imageUrl());
            job.setCompletedAt(LocalDateTime.now());
            jobMapper.updateById(job);
            increaseTagUsage(selectedTagIds);
            return new GenerationDtos.GenerateResponse(job.getId(), firstArtwork.artworkId(), firstArtwork.imageUrl(), job.getStatus(),
                    firstArtwork.title(), generatedArtworks);
        } catch (BusinessException ex) {
            failJob(job, ex.getMessage());
            throw ex;
        } catch (RuntimeException ex) {
            failJob(job, ex.getMessage());
            throw new BusinessException(ErrorCode.SD_UNAVAILABLE, ex.getMessage());
        }
    }

    @Override
    public GenerationDtos.GenerationJobView getJob(Long userId, Long jobId) {
        AiGenerationJob job = jobMapper.selectOne(Wrappers.<AiGenerationJob>lambdaQuery()
                .eq(AiGenerationJob::getId, jobId)
                .eq(AiGenerationJob::getUserId, userId));
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Generation job not found");
        }
        List<GenerationDtos.GeneratedArtwork> artworks = artworkMapper.selectList(Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getSourceJobId, job.getId())
                .eq(Artwork::getUserId, userId)
                .orderByAsc(Artwork::getCreatedAt))
                .stream()
                .map(artwork -> new GenerationDtos.GeneratedArtwork(artwork.getId(), artwork.getImageUrl(), artwork.getTitle()))
                .toList();
        return new GenerationDtos.GenerationJobView(job.getId(), job.getProvider(), job.getStatus(),
                titleFromParams(job.getParamsJson()), job.getImageUrl(), job.getPromptText(), job.getNegativePrompt(), job.getParamsJson(), job.getErrorMessage(), artworks,
                job.getCreatedAt(), job.getCompletedAt());
    }

    @Override
    public List<GenerationDtos.GenerationJobView> myJobs(Long userId, int page, int size) {
        Page<AiGenerationJob> result = jobMapper.selectPage(new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 30)),
                Wrappers.<AiGenerationJob>lambdaQuery()
                        .eq(AiGenerationJob::getUserId, userId)
                        .orderByDesc(AiGenerationJob::getCreatedAt));
        return result.getRecords().stream()
                .map(job -> toJobView(job, userId))
                .toList();
    }

    @Override
    public GenerationDtos.GenerationJobView cancelJob(Long userId, Long jobId) {
        AiGenerationJob job = jobMapper.selectOne(Wrappers.<AiGenerationJob>lambdaQuery()
                .eq(AiGenerationJob::getId, jobId)
                .eq(AiGenerationJob::getUserId, userId));
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Generation job not found");
        }
        if (!List.of("COMPLETED", "FAILED", "CANCELLED").contains(job.getStatus())) {
            Future<?> future = activeTasks.remove(jobId);
            if (future != null) {
                future.cancel(false);
            }
            job.setStatus("CANCELLED");
            job.setCompletedAt(LocalDateTime.now());
            jobMapper.updateById(job);
        }
        return toJobView(jobMapper.selectById(jobId), userId);
    }

    private GenerationDtos.GenerationJobView toJobView(AiGenerationJob job, Long userId) {
        List<GenerationDtos.GeneratedArtwork> artworks = artworkMapper.selectList(Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getSourceJobId, job.getId())
                .eq(Artwork::getUserId, userId)
                .orderByAsc(Artwork::getCreatedAt))
                .stream()
                .map(artwork -> new GenerationDtos.GeneratedArtwork(artwork.getId(), artwork.getImageUrl(), artwork.getTitle()))
                .toList();
        return new GenerationDtos.GenerationJobView(job.getId(), job.getProvider(), job.getStatus(),
                titleFromParams(job.getParamsJson()), job.getImageUrl(), job.getPromptText(), job.getNegativePrompt(), job.getParamsJson(), job.getErrorMessage(), artworks,
                job.getCreatedAt(), job.getCompletedAt());
    }

    @Override
    public Map<String, Object> providerOptions() {
        Map<String, Object> options = get("/sdapi/v1/options", new ParameterizedTypeReference<>() {
        });
        return options == null ? Map.of() : options;
    }

    @Override
    public List<Map<String, Object>> models() {
        List<Map<String, Object>> models = get("/sdapi/v1/sd-models", new ParameterizedTypeReference<>() {
        });
        return models == null ? List.of() : models;
    }

    @Override
    public List<Map<String, Object>> samplers() {
        List<Map<String, Object>> samplers = get("/sdapi/v1/samplers", new ParameterizedTypeReference<>() {
        });
        return samplers == null ? List.of() : samplers;
    }

    @Override
    public List<Map<String, Object>> loras() {
        List<Map<String, Object>> loras = get("/sdapi/v1/loras", new ParameterizedTypeReference<>() {
        });
        return loras == null ? List.of() : loras;
    }

    @Override
    public List<Map<String, Object>> vaes() {
        List<Map<String, Object>> vaes = tryGetList("/sdapi/v1/sd-modules");
        if (!vaes.isEmpty()) {
            return vaes;
        }

        vaes = tryGetList("/sdapi/v1/sd-vae");
        if (!vaes.isEmpty()) {
            return vaes;
        }

        Map<String, Object> options = providerOptions();
        Object currentVae = options.get("sd_vae");
        if (currentVae instanceof String name && StringUtils.hasText(name)) {
            return List.of(Map.of("name", name, "title", name));
        }
        return List.of();
    }

    @Override
    public List<Map<String, Object>> upscalers() {
        List<Map<String, Object>> upscalers = get("/sdapi/v1/upscalers", new ParameterizedTypeReference<>() {
        });
        return upscalers == null ? List.of() : upscalers;
    }

    private <T> T get(String path, ParameterizedTypeReference<T> typeReference) {
        try {
            return webClient()
                    .get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(typeReference)
                    .block(Duration.ofSeconds(Math.min(timeoutSeconds, 30)));
        } catch (WebClientRequestException | WebClientResponseException ex) {
            throw new BusinessException(ErrorCode.SD_UNAVAILABLE, ex.getMessage());
        }
    }

    private List<Map<String, Object>> tryGetList(String path) {
        try {
            List<Map<String, Object>> result = get(path, new ParameterizedTypeReference<>() {
            });
            return result == null ? List.of() : result;
        } catch (BusinessException ex) {
            return List.of();
        }
    }

    private GenerationDtos.SdTxt2ImgResponse callGeneration(GenerationDtos.GenerateRequest request, boolean imageToImage) {
        try {
            String body = webClient()
                    .post()
                    .uri(imageToImage ? "/sdapi/v1/img2img" : "/sdapi/v1/txt2img")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(buildPayload(request, imageToImage))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(timeoutSeconds));
            return objectMapper.readValue(body, GenerationDtos.SdTxt2ImgResponse.class);
        } catch (WebClientRequestException | WebClientResponseException ex) {
            throw new BusinessException(ErrorCode.SD_UNAVAILABLE, ex.getMessage());
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.SD_UNAVAILABLE, "Could not parse generation provider response: " + ex.getOriginalMessage());
        }
    }

    private Map<String, Object> buildPayload(GenerationDtos.GenerateRequest request, boolean imageToImage) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("prompt", composePromptWithLoras(request));
        payload.put("negative_prompt", nullToEmpty(request.negativePrompt()));
        if (imageToImage) {
            payload.put("init_images", request.initImages());
            payload.put("resize_mode", clampInt(request.resizeMode(), 0, 3, 0));
            payload.put("denoising_strength", request.denoisingStrength() == null ? 0.55 : request.denoisingStrength());
        }
        payload.put("width", request.width() == null ? 768 : request.width());
        payload.put("height", request.height() == null ? 768 : request.height());
        payload.put("steps", request.steps() == null ? 24 : request.steps());
        payload.put("cfg_scale", request.cfgScale() == null ? 7.0 : request.cfgScale());
        payload.put("sampler_name", StringUtils.hasText(request.samplerName()) ? request.samplerName() : "DPM++ 2M");
        payload.put("seed", request.seed() == null ? -1 : BigDecimal.valueOf(request.seed()));
        payload.put("restore_faces", Boolean.TRUE.equals(request.restoreFaces()));
        payload.put("tiling", Boolean.TRUE.equals(request.tiling()));
        payload.put("batch_size", clampInt(request.batchSize(), 1, 4, 1));
        payload.put("n_iter", clampInt(request.batchCount(), 1, 4, 1));
        payload.put("send_images", true);
        payload.put("save_images", false);
        payload.put("enable_hr", Boolean.TRUE.equals(request.enableHr()));
        if (Boolean.TRUE.equals(request.enableHr())) {
            payload.put("denoising_strength", request.denoisingStrength() == null ? 0.35 : request.denoisingStrength());
            payload.put("hr_scale", request.hrScale() == null ? 1.5 : request.hrScale());
            payload.put("hr_upscaler", StringUtils.hasText(request.hrUpscaler()) ? request.hrUpscaler() : "Latent");
            if (request.hrSecondPassSteps() != null && request.hrSecondPassSteps() > 0) {
                payload.put("hr_second_pass_steps", request.hrSecondPassSteps());
            }
        }

        Map<String, Object> overrideSettings = new LinkedHashMap<>();
        if (request.overrideSettings() != null) {
            overrideSettings.putAll(request.overrideSettings());
        }
        if (StringUtils.hasText(request.model())) {
            overrideSettings.put("sd_model_checkpoint", request.model());
        }
        if (StringUtils.hasText(request.vae())) {
            overrideSettings.put("sd_vae", request.vae());
        }
        if (request.clipSkip() != null) {
            overrideSettings.put("CLIP_stop_at_last_layers", clampInt(request.clipSkip(), 1, 12, 1));
        }
        if (!overrideSettings.isEmpty()) {
            payload.put("override_settings", overrideSettings);
            payload.put("override_settings_restore_afterwards", true);
        }
        if (request.extraPayload() != null) {
            mergeExtraPayload(payload, request.extraPayload());
        }
        return payload;
    }

    private void mergeExtraPayload(Map<String, Object> payload, Map<String, Object> extraPayload) {
        Set<String> protectedKeys = Set.of(
                "prompt", "negative_prompt", "init_images", "width", "height", "steps", "cfg_scale",
                "sampler_name", "seed", "restore_faces", "tiling", "batch_size", "n_iter",
                "send_images", "save_images", "enable_hr", "denoising_strength", "hr_scale",
                "hr_upscaler", "hr_second_pass_steps", "resize_mode", "override_settings",
                "override_settings_restore_afterwards");
        for (Map.Entry<String, Object> entry : extraPayload.entrySet()) {
            if (entry.getKey() == null || protectedKeys.contains(entry.getKey())) {
                continue;
            }
            payload.put(entry.getKey(), entry.getValue());
        }
    }

    private String composePromptWithLoras(GenerationDtos.GenerateRequest request) {
        StringBuilder prompt = new StringBuilder(request.prompt().trim());
        if (request.loras() == null || request.loras().isEmpty()) {
            return prompt.toString();
        }
        for (GenerationDtos.LoraSelection lora : request.loras()) {
            if (lora == null || !StringUtils.hasText(lora.name())) {
                continue;
            }
            String cleanName = lora.name().replace("<", "").replace(">", "").trim();
            if (!StringUtils.hasText(cleanName)) {
                continue;
            }
            double weight = lora.weight() == null ? 0.75 : Math.max(0.0, Math.min(2.0, lora.weight()));
            prompt.append(", <lora:").append(cleanName).append(':').append(formatWeight(weight)).append('>');
        }
        return prompt.toString();
    }

    private String buildArtworkTitle(String title, int index, int total) {
        String baseTitle = StringUtils.hasText(title) ? title.trim() : "Untitled generation";
        if (total <= 1) {
            return baseTitle;
        }
        return baseTitle + " #" + (index + 1);
    }

    private Map<String, Object> safeRequestForStorage(GenerationDtos.GenerateRequest request) {
        Map<String, Object> value = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        Object initImages = value.get("initImages");
        if (initImages instanceof List<?> images && !images.isEmpty()) {
            value.put("initImages", List.of("[base64 image omitted]"));
        }
        return value;
    }

    private List<Long> selectedPublicTagIds(GenerationDtos.GenerateRequest request) {
        if (request.tagIds() == null || request.tagIds().isEmpty()) {
            return List.of();
        }
        List<Long> requested = request.tagIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (requested.isEmpty()) {
            return List.of();
        }
        Set<Long> valid = new HashSet<>(tagMapper.selectList(Wrappers.<Tag>lambdaQuery()
                .in(Tag::getId, requested)
                .eq(Tag::getStatus, "ACTIVE")
                .eq(Tag::getVisibility, "PUBLIC"))
                .stream()
                .map(Tag::getId)
                .toList());
        return requested.stream().filter(valid::contains).toList();
    }

    private void saveArtworkTags(Artwork artwork, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArtworkTag link = new ArtworkTag();
            link.setArtworkId(artwork.getId());
            link.setTagId(tagId);
            link.setUserId(artwork.getUserId());
            link.setCreatedAt(LocalDateTime.now());
            artworkTagMapper.insert(link);
        }
    }

    private void increaseTagUsage(List<Long> tagIds) {
        for (Long tagId : tagIds) {
            tagMapper.update(null, Wrappers.<Tag>lambdaUpdate()
                    .eq(Tag::getId, tagId)
                    .setSql("usage_count = usage_count + 1"));
        }
    }

    private boolean jobWasCancelled(Long jobId) {
        AiGenerationJob current = jobMapper.selectById(jobId);
        return current != null && "CANCELLED".equals(current.getStatus());
    }

    private String titleFromParams(String paramsJson) {
        if (!StringUtils.hasText(paramsJson)) {
            return "Untitled generation";
        }
        try {
            String title = objectMapper.readTree(paramsJson).path("title").asText();
            return StringUtils.hasText(title) ? title : "Untitled generation";
        } catch (JsonProcessingException ex) {
            return "Untitled generation";
        }
    }

    private int clampInt(Integer value, int min, int max, int fallback) {
        if (value == null) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    private String formatWeight(double weight) {
        String text = String.format(java.util.Locale.US, "%.2f", weight);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(this::configureLargeBuffer)
                .build();
        return WebClient.builder()
                .baseUrl(sdBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    private void configureLargeBuffer(ClientCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(128 * 1024 * 1024);
    }

    private void failJob(AiGenerationJob job, String message) {
        if (jobWasCancelled(job.getId())) {
            job.setStatus("CANCELLED");
            job.setCompletedAt(LocalDateTime.now());
            jobMapper.updateById(job);
            return;
        }
        job.setStatus("FAILED");
        job.setErrorMessage(message);
        job.setCompletedAt(LocalDateTime.now());
        jobMapper.updateById(job);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @PreDestroy
    public void shutdownExecutor() {
        generationExecutor.shutdownNow();
    }

    private static class GenerationThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "aiart-generation-worker");
            thread.setDaemon(true);
            return thread;
        }
    }
}
