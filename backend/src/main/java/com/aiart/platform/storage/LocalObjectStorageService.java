package com.aiart.platform.storage;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Service
@ConditionalOnProperty(prefix = "aiart.storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageService implements ObjectStorageService {
    private final Path root;

    public LocalObjectStorageService(StorageProperties properties) {
        this.root = Path.of(properties.getUploadRoot()).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject storePublic(String key, String contentType, InputStream inputStream, long size) {
        return store(true, key, inputStream);
    }

    @Override
    public StoredObject storePrivate(String key, String contentType, InputStream inputStream, long size) {
        return store(false, key, inputStream);
    }

    @Override
    public StorageContent open(String reference) {
        try {
            Path target = resolve(reference);
            if (!Files.isRegularFile(target)) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "Stored file not found");
            }
            String contentType = Files.probeContentType(target);
            return new StorageContent(Files.newInputStream(target), Files.size(target),
                    contentType == null ? "application/octet-stream" : contentType);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    @Override
    public URI temporaryDownloadUrl(String reference, Duration expiry) {
        return null;
    }

    @Override
    public void delete(String reference) {
        try {
            Files.deleteIfExists(resolve(reference));
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    private StoredObject store(boolean publicObject, String key, InputStream inputStream) {
        String reference = StorageReferenceCodec.reference(publicObject, key);
        try {
            Path target = resolve(reference);
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredObject(reference, publicObject ? StorageReferenceCodec.publicUrl(reference) : null);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    private Path resolve(String reference) {
        Path target = root.resolve(StorageReferenceCodec.objectName(reference)).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Storage path escaped its root");
        }
        return target;
    }
}
