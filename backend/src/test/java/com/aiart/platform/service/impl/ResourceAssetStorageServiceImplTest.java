package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.service.ResourceAssetStorageService;
import com.aiart.platform.storage.LocalObjectStorageService;
import com.aiart.platform.storage.StorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceAssetStorageServiceImplTest {
    @TempDir
    Path tempDir;

    @Test
    void storesSupportedResourceAsPrivateObject() {
        ResourceAssetStorageService service = service(1024);
        MockMultipartFile file = new MockMultipartFile(
                "file", "tree.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2});

        ResourceAssetStorageService.UploadedAsset uploaded = service.save(file);

        assertTrue(uploaded.reference().startsWith("storage://private/style-assets/"));
        assertEquals("tree.png", uploaded.originalFilename());
        assertEquals(10, uploaded.size());
    }

    @Test
    void rejectsUnsupportedOrOversizedFiles() {
        ResourceAssetStorageService service = service(3);
        MockMultipartFile oversized = new MockMultipartFile(
                "file", "tree.png", "image/png", new byte[]{1, 2, 3, 4});
        MockMultipartFile executable = new MockMultipartFile(
                "file", "tool.exe", "application/octet-stream", new byte[]{1});

        assertThrows(BusinessException.class, () -> service.save(oversized));
        assertThrows(BusinessException.class, () -> service.save(executable));
    }

    @Test
    void rejectsSpoofedContentAndMismatchedMimeType() {
        ResourceAssetStorageService service = service(1024);
        MockMultipartFile spoofedPng = new MockMultipartFile(
                "file", "tree.png", "image/png", "not a png".getBytes());
        MockMultipartFile mismatchedMime = new MockMultipartFile(
                "file", "tree.png", "image/jpeg",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        assertThrows(BusinessException.class, () -> service.save(spoofedPng));
        assertThrows(BusinessException.class, () -> service.save(mismatchedMime));
    }

    private ResourceAssetStorageService service(long maxBytes) {
        StorageProperties properties = new StorageProperties();
        properties.setUploadRoot(tempDir.toString());
        properties.setMaxAssetBytes(maxBytes);
        return new ResourceAssetStorageServiceImpl(new LocalObjectStorageService(properties), properties);
    }
}
