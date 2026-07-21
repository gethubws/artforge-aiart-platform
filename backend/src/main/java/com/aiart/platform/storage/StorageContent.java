package com.aiart.platform.storage;

import java.io.IOException;
import java.io.InputStream;

public record StorageContent(InputStream inputStream, long contentLength, String contentType) implements AutoCloseable {
    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
