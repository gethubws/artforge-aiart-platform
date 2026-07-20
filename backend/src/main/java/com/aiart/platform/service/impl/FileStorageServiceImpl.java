package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public String saveTagPreview(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Preview image is required");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only image files are supported");
        }
        try {
            LocalDate today = LocalDate.now();
            Path dir = Path.of(uploadRoot, "tag-previews", String.valueOf(today.getYear()), two(today.getMonthValue()));
            Files.createDirectories(dir);
            String extension = extension(file.getOriginalFilename(), contentType);
            String filename = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/tag-previews/" + today.getYear() + "/" + two(today.getMonthValue()) + "/" + filename;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    @Override
    public void deleteStoredFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            Path root = Path.of(uploadRoot).toAbsolutePath().normalize();
            Path target = root.resolve(fileUrl.substring("/uploads/".length())).normalize();
            if (target.startsWith(root)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ex) {
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

    private String extension(String filename, String contentType) {
        if (StringUtils.hasText(filename)) {
            String clean = Path.of(filename).getFileName().toString();
            int dot = clean.lastIndexOf('.');
            if (dot >= 0 && dot < clean.length() - 1) {
                String value = clean.substring(dot).toLowerCase();
                if (value.matches("\\.(png|jpe?g|webp|gif)")) {
                    return value;
                }
            }
        }
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".png";
        };
    }
}
