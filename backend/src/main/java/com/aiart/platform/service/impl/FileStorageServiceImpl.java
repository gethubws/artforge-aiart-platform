package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.service.FileStorageService;
import com.aiart.platform.storage.ObjectStorageService;
import com.aiart.platform.storage.StorageReferenceCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private final ObjectStorageService objectStorageService;
    private final com.aiart.platform.storage.StorageProperties storageProperties;

    @Override
    public String saveBase64Png(String base64Image) {
        try {
            String cleaned = clean(base64Image);
            byte[] bytes = Base64.getDecoder().decode(cleaned);
            LocalDate today = LocalDate.now();
            String filename = UUID.randomUUID() + ".png";
            String key = "generated/" + today.getYear() + "/" + two(today.getMonthValue()) + "/"
                    + two(today.getDayOfMonth()) + "/" + filename;
            return objectStorageService.storePublic(key, "image/png", new ByteArrayInputStream(bytes), bytes.length)
                    .publicUrl();
        } catch (IllegalArgumentException ex) {
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
        if (file.getSize() > storageProperties.getMaxAssetBytes()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Preview image exceeds the configured size limit");
        }
        try {
            LocalDate today = LocalDate.now();
            String extension = extension(file.getOriginalFilename(), contentType);
            validateImageFile(file, extension, contentType);
            String filename = UUID.randomUUID() + extension;
            String key = "tag-previews/" + today.getYear() + "/" + two(today.getMonthValue()) + "/" + filename;
            return objectStorageService.storePublic(key, contentType, file.getInputStream(), file.getSize()).publicUrl();
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    @Override
    public void deleteStoredFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }
        String managedReference = StorageReferenceCodec.referenceFromPublicUrl(fileUrl);
        if (managedReference != null) {
            objectStorageService.delete(managedReference);
            return;
        }
        if (!fileUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            Path root = Path.of(storageProperties.getUploadRoot()).toAbsolutePath().normalize();
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

    private void validateImageFile(MultipartFile file, String extension, String contentType) {
        String normalizedType = contentType.toLowerCase();
        boolean typeMatches = switch (extension) {
            case ".png" -> "image/png".equals(normalizedType);
            case ".jpg", ".jpeg" -> "image/jpeg".equals(normalizedType);
            case ".webp" -> "image/webp".equals(normalizedType);
            case ".gif" -> "image/gif".equals(normalizedType);
            default -> false;
        };
        if (!typeMatches) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Preview image extension does not match its content type");
        }
        byte[] header = new byte[12];
        try (InputStream inputStream = file.getInputStream()) {
            int length = inputStream.read(header);
            boolean signatureMatches = switch (extension) {
                case ".png" -> length >= 8
                        && Byte.toUnsignedInt(header[0]) == 0x89
                        && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                        && Byte.toUnsignedInt(header[4]) == 0x0D && Byte.toUnsignedInt(header[5]) == 0x0A
                        && Byte.toUnsignedInt(header[6]) == 0x1A && Byte.toUnsignedInt(header[7]) == 0x0A;
                case ".jpg", ".jpeg" -> length >= 3
                        && Byte.toUnsignedInt(header[0]) == 0xFF
                        && Byte.toUnsignedInt(header[1]) == 0xD8
                        && Byte.toUnsignedInt(header[2]) == 0xFF;
                case ".gif" -> length >= 6
                        && header[0] == 'G' && header[1] == 'I' && header[2] == 'F'
                        && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
                case ".webp" -> length >= 12
                        && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
                default -> false;
            };
            if (!signatureMatches) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Preview image content does not match its declared type");
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }
}
