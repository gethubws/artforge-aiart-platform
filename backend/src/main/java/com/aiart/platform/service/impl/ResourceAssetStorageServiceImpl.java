package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.service.ResourceAssetStorageService;
import com.aiart.platform.storage.ObjectStorageService;
import com.aiart.platform.storage.StorageContent;
import com.aiart.platform.storage.StorageProperties;
import com.aiart.platform.storage.StorageReferenceCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceAssetStorageServiceImpl implements ResourceAssetStorageService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif",
            "application/zip",
            "application/x-zip-compressed",
            "application/json",
            "text/plain");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".webp", ".gif", ".zip", ".json", ".txt");

    private final ObjectStorageService objectStorageService;
    private final StorageProperties storageProperties;

    @Override
    public UploadedAsset save(MultipartFile file) {
        validate(file);
        String originalFilename = safeFilename(file.getOriginalFilename());
        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String extension = extension(originalFilename);
        LocalDate today = LocalDate.now();
        String key = "style-assets/" + today.getYear() + "/" + two(today.getMonthValue()) + "/"
                + UUID.randomUUID() + extension;
        try {
            String reference = objectStorageService
                    .storePrivate(key, contentType, file.getInputStream(), file.getSize())
                    .reference();
            return new UploadedAsset(reference, originalFilename, contentType, file.getSize());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    @Override
    public StorageContent open(String reference) {
        if (!isManagedReference(reference)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported resource file reference");
        }
        return objectStorageService.open(reference);
    }

    @Override
    public URI temporaryDownloadUrl(String reference) {
        if (!isManagedReference(reference)) {
            return null;
        }
        return objectStorageService.temporaryDownloadUrl(
                reference,
                Duration.ofSeconds(storageProperties.getDownloadExpirySeconds()));
    }

    @Override
    public boolean isManagedReference(String reference) {
        return StorageReferenceCodec.isManaged(reference) && !StorageReferenceCodec.isPublic(reference);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Resource file is required");
        }
        if (file.getSize() > storageProperties.getMaxAssetBytes()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Resource file exceeds the configured size limit");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)
                || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported resource file type");
        }
        String extension = extension(safeFilename(file.getOriginalFilename()));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported resource file extension");
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (!matchesDeclaredType(extension, normalizedContentType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Resource file extension does not match its content type");
        }
        if (!hasExpectedSignature(file, extension)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Resource file content does not match its declared type");
        }
    }

    private boolean matchesDeclaredType(String extension, String contentType) {
        return switch (extension) {
            case ".png" -> "image/png".equals(contentType);
            case ".jpg", ".jpeg" -> "image/jpeg".equals(contentType);
            case ".webp" -> "image/webp".equals(contentType);
            case ".gif" -> "image/gif".equals(contentType);
            case ".zip" -> "application/zip".equals(contentType)
                    || "application/x-zip-compressed".equals(contentType);
            case ".json" -> "application/json".equals(contentType);
            case ".txt" -> "text/plain".equals(contentType);
            default -> false;
        };
    }

    private boolean hasExpectedSignature(MultipartFile file, String extension) {
        if (Set.of(".json", ".txt").contains(extension)) {
            return true;
        }
        byte[] header = new byte[12];
        try (InputStream inputStream = file.getInputStream()) {
            int length = inputStream.read(header);
            return switch (extension) {
                case ".png" -> length >= 8
                        && unsigned(header[0]) == 0x89
                        && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                        && unsigned(header[4]) == 0x0D && unsigned(header[5]) == 0x0A
                        && unsigned(header[6]) == 0x1A && unsigned(header[7]) == 0x0A;
                case ".jpg", ".jpeg" -> length >= 3
                        && unsigned(header[0]) == 0xFF
                        && unsigned(header[1]) == 0xD8
                        && unsigned(header[2]) == 0xFF;
                case ".gif" -> length >= 6
                        && header[0] == 'G' && header[1] == 'I' && header[2] == 'F'
                        && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
                case ".webp" -> length >= 12
                        && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
                case ".zip" -> length >= 4 && header[0] == 'P' && header[1] == 'K'
                        && ((unsigned(header[2]) == 0x03 && unsigned(header[3]) == 0x04)
                        || (unsigned(header[2]) == 0x05 && unsigned(header[3]) == 0x06)
                        || (unsigned(header[2]) == 0x07 && unsigned(header[3]) == 0x08));
                default -> false;
            };
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    private int unsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }

    private String safeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "resource.bin";
        }
        return Path.of(filename).getFileName().toString();
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    private String two(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}
