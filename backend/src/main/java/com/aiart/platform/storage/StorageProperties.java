package com.aiart.platform.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aiart.storage")
public class StorageProperties {
    private Provider provider = Provider.LOCAL;
    private String uploadRoot = "uploads";
    private long maxAssetBytes = 100L * 1024L * 1024L;
    private int downloadExpirySeconds = 300;
    private Minio minio = new Minio();

    public enum Provider {
        LOCAL,
        MINIO
    }

    @Getter
    @Setter
    public static class Minio {
        private String endpoint = "http://127.0.0.1:9000";
        private String publicEndpoint = "http://127.0.0.1:9000";
        private String accessKey = "artforge";
        private String secretKey = "artforge-local-secret";
        private String bucket = "artforge";
    }
}
