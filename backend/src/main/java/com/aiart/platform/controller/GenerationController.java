package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.GenerationDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.GenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
public class GenerationController {
    private final GenerationService generationService;
    private final CurrentUser currentUser;

    @PostMapping("/txt2img")
    public ApiResponse<GenerationDtos.GenerateResponse> txt2img(@Valid @RequestBody GenerationDtos.GenerateRequest request) {
        return ApiResponse.ok(generationService.generate(currentUser.requireUserId(), request));
    }

    @PostMapping("/queue/txt2img")
    public ApiResponse<GenerationDtos.QueuedGenerationResponse> queueTxt2img(@Valid @RequestBody GenerationDtos.GenerateRequest request) {
        return ApiResponse.ok(generationService.enqueueTxt2Img(currentUser.requireUserId(), request));
    }

    @PostMapping("/img2img")
    public ApiResponse<GenerationDtos.GenerateResponse> img2img(@Valid @RequestBody GenerationDtos.GenerateRequest request) {
        return ApiResponse.ok(generationService.generateImg2Img(currentUser.requireUserId(), request));
    }

    @PostMapping("/queue/img2img")
    public ApiResponse<GenerationDtos.QueuedGenerationResponse> queueImg2img(@Valid @RequestBody GenerationDtos.GenerateRequest request) {
        return ApiResponse.ok(generationService.enqueueImg2Img(currentUser.requireUserId(), request));
    }

    @GetMapping("/jobs/{jobId}")
    public ApiResponse<GenerationDtos.GenerationJobView> job(@PathVariable Long jobId) {
        return ApiResponse.ok(generationService.getJob(currentUser.requireUserId(), jobId));
    }

    @GetMapping("/jobs/my")
    public ApiResponse<List<GenerationDtos.GenerationJobView>> myJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.ok(generationService.myJobs(currentUser.requireUserId(), page, size));
    }

    @PostMapping("/jobs/{jobId}/cancel")
    public ApiResponse<GenerationDtos.GenerationJobView> cancelJob(@PathVariable Long jobId) {
        return ApiResponse.ok(generationService.cancelJob(currentUser.requireUserId(), jobId));
    }

    @GetMapping("/provider/options")
    public ApiResponse<Map<String, Object>> options() {
        return ApiResponse.ok(generationService.providerOptions());
    }

    @GetMapping("/provider/models")
    public ApiResponse<List<Map<String, Object>>> models() {
        return ApiResponse.ok(generationService.models());
    }

    @GetMapping("/provider/samplers")
    public ApiResponse<List<Map<String, Object>>> samplers() {
        return ApiResponse.ok(generationService.samplers());
    }

    @GetMapping("/provider/loras")
    public ApiResponse<List<Map<String, Object>>> loras() {
        return ApiResponse.ok(generationService.loras());
    }

    @GetMapping("/provider/vaes")
    public ApiResponse<List<Map<String, Object>>> vaes() {
        return ApiResponse.ok(generationService.vaes());
    }

    @GetMapping("/provider/upscalers")
    public ApiResponse<List<Map<String, Object>>> upscalers() {
        return ApiResponse.ok(generationService.upscalers());
    }
}
