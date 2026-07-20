package com.aiart.platform.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveBase64Png(String base64Image);

    String saveTagPreview(MultipartFile file);

    void deleteStoredFile(String fileUrl);
}
