package com.aiart.platform.service;

import com.aiart.platform.storage.StorageContent;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

public interface ResourceAssetStorageService {
    UploadedAsset save(MultipartFile file);

    StorageContent open(String reference);

    URI temporaryDownloadUrl(String reference);

    boolean isManagedReference(String reference);

    record UploadedAsset(String reference, String originalFilename, String contentType, long size) {
    }
}
