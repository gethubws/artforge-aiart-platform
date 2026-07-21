package com.aiart.platform.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalObjectStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void storesPublicAndPrivateObjectsWithDifferentExposure() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setUploadRoot(tempDir.toString());
        LocalObjectStorageService storage = new LocalObjectStorageService(properties);
        byte[] content = "artforge".getBytes(StandardCharsets.UTF_8);

        StoredObject publicObject = storage.storePublic(
                "generated/example.txt", "text/plain", new ByteArrayInputStream(content), content.length);
        StoredObject privateObject = storage.storePrivate(
                "style-assets/example.txt", "text/plain", new ByteArrayInputStream(content), content.length);

        assertTrue(publicObject.publicUrl().startsWith("/api/public/files/"));
        assertNull(privateObject.publicUrl());
        try (StorageContent stored = storage.open(privateObject.reference())) {
            assertArrayEquals(content, stored.inputStream().readAllBytes());
        }

        storage.delete(privateObject.reference());
        assertFalse(tempDir.resolve("private/style-assets/example.txt").toFile().exists());
    }
}
