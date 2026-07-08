package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${aiart.storage.upload-root:uploads}")
    private String uploadRoot;

    @Override
    public String saveBase64Png(String base64Image) {
        try {
            String cleaned = clean(base64Image);
            byte[] bytes = Base64.getDecoder().decode(cleaned);
            LocalDate today = LocalDate.now();
            Path dir = Path.of(uploadRoot, "generated", String.valueOf(today.getYear()), two(today.getMonthValue()), two(today.getDayOfMonth()));
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + ".png";
            Path target = dir.resolve(filename);
            Files.write(target, bytes);
            return "/uploads/generated/" + today.getYear() + "/" + two(today.getMonthValue()) + "/" + two(today.getDayOfMonth()) + "/" + filename;
        } catch (IllegalArgumentException | IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    private String clean(String base64Image) {
        int comma = base64Image.indexOf(',');
        return comma >= 0 ? base64Image.substring(comma + 1) : base64Image;
    }

    private String two(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}
