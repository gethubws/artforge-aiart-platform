package com.aiart.platform.controller;

import com.aiart.platform.storage.ObjectStorageService;
import com.aiart.platform.storage.StorageContent;
import com.aiart.platform.storage.StorageReferenceCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/files")
@RequiredArgsConstructor
public class PublicFileController {
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{token}")
    public ResponseEntity<InputStreamResource> file(@PathVariable String token) {
        String reference = StorageReferenceCodec.referenceFromPublicToken(token);
        StorageContent content = objectStorageService.open(reference);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.contentLength())
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(new InputStreamResource(content.inputStream()));
    }
}
