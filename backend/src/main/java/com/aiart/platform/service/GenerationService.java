package com.aiart.platform.service;

import com.aiart.platform.dto.GenerationDtos;

import java.util.List;
import java.util.Map;

public interface GenerationService {
    GenerationDtos.GenerateResponse generate(Long userId, GenerationDtos.GenerateRequest request);

    GenerationDtos.QueuedGenerationResponse enqueueTxt2Img(Long userId, GenerationDtos.GenerateRequest request);

    GenerationDtos.GenerateResponse generateImg2Img(Long userId, GenerationDtos.GenerateRequest request);

    GenerationDtos.QueuedGenerationResponse enqueueImg2Img(Long userId, GenerationDtos.GenerateRequest request);

    GenerationDtos.GenerationJobView getJob(Long userId, Long jobId);

    List<GenerationDtos.GenerationJobView> myJobs(Long userId, int page, int size);

    GenerationDtos.GenerationJobView cancelJob(Long userId, Long jobId);

    Map<String, Object> providerOptions();

    List<Map<String, Object>> models();

    List<Map<String, Object>> samplers();

    List<Map<String, Object>> loras();

    List<Map<String, Object>> vaes();

    List<Map<String, Object>> upscalers();
}
