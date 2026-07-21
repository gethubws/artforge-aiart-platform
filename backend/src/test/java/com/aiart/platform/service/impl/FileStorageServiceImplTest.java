package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.storage.ObjectStorageService;
import com.aiart.platform.storage.StorageProperties;
import com.aiart.platform.storage.StoredObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStorageServiceImplTest {
    private ObjectStorageService objectStorageService;
    private FileStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        objectStorageService = mock(ObjectStorageService.class);
        StorageProperties properties = new StorageProperties();
        properties.setMaxAssetBytes(1024);
        service = new FileStorageServiceImpl(objectStorageService, properties);
    }

    @Test
    void storesVerifiedTagPreviewAsPublicObject() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "preview.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1});
        when(objectStorageService.storePublic(any(), eq("image/png"), any(InputStream.class), anyLong()))
                .thenReturn(new StoredObject("storage://public/tag-previews/preview.png", "/api/public/files/token"));

        String url = service.saveTagPreview(file);

        assertEquals("/api/public/files/token", url);
        verify(objectStorageService).storePublic(any(), eq("image/png"), any(InputStream.class), eq(9L));
    }

    @Test
    void rejectsSpoofedOrMismatchedTagPreview() {
        MockMultipartFile spoofed = new MockMultipartFile(
                "file", "preview.png", "image/png", "not an image".getBytes());
        MockMultipartFile mismatched = new MockMultipartFile(
                "file", "preview.jpg", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        assertThrows(BusinessException.class, () -> service.saveTagPreview(spoofed));
        assertThrows(BusinessException.class, () -> service.saveTagPreview(mismatched));
    }
}
