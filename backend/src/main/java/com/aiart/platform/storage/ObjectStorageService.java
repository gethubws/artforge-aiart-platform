package com.aiart.platform.storage;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

public interface ObjectStorageService {
    StoredObject storePublic(String key, String contentType, InputStream inputStream, long size);

    StoredObject storePrivate(String key, String contentType, InputStream inputStream, long size);

    StorageContent open(String reference);

    URI temporaryDownloadUrl(String reference, Duration expiry);

    void delete(String reference);
}
