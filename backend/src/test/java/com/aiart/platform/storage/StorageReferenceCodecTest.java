package com.aiart.platform.storage;

import com.aiart.platform.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageReferenceCodecTest {
    @Test
    void publicReferenceRoundTripsThroughUrlToken() {
        String reference = StorageReferenceCodec.reference(true, "generated/2026/07/image.png");
        String publicUrl = StorageReferenceCodec.publicUrl(reference);

        assertTrue(publicUrl.startsWith("/api/public/files/"));
        assertEquals(reference, StorageReferenceCodec.referenceFromPublicUrl(publicUrl));
        assertEquals("public/generated/2026/07/image.png", StorageReferenceCodec.objectName(reference));
    }

    @Test
    void privateReferenceCannotBecomePublicUrl() {
        String reference = StorageReferenceCodec.reference(false, "style-assets/resource.zip");

        assertThrows(BusinessException.class, () -> StorageReferenceCodec.publicUrl(reference));
    }

    @Test
    void parentPathSegmentsAreRejected() {
        assertThrows(BusinessException.class, () -> StorageReferenceCodec.reference(true, "images/../secret.txt"));
        assertThrows(BusinessException.class, () -> StorageReferenceCodec.reference(true, "images/.."));
    }
}
