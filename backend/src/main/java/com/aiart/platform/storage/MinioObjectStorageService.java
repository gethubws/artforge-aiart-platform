package com.aiart.platform.storage;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

@Service
@ConditionalOnProperty(prefix = "aiart.storage", name = "provider", havingValue = "minio")
public class MinioObjectStorageService implements ObjectStorageService {
    private final MinioClient client;
    private final String bucket;
    private final URI publicEndpoint;

    public MinioObjectStorageService(StorageProperties properties) {
        StorageProperties.Minio config = properties.getMinio();
        this.bucket = config.getBucket();
        this.publicEndpoint = URI.create(config.getPublicEndpoint());
        this.client = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize MinIO bucket", ex);
        }
    }

    @Override
    public StoredObject storePublic(String key, String contentType, InputStream inputStream, long size) {
        return store(true, key, contentType, inputStream, size);
    }

    @Override
    public StoredObject storePrivate(String key, String contentType, InputStream inputStream, long size) {
        return store(false, key, contentType, inputStream, size);
    }

    @Override
    public StorageContent open(String reference) {
        String objectName = StorageReferenceCodec.objectName(reference);
        try {
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            GetObjectResponse response = client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            String contentType = stat.contentType() == null ? "application/octet-stream" : stat.contentType();
            return new StorageContent(response, stat.size(), contentType);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Stored object is unavailable");
        }
    }

    @Override
    public URI temporaryDownloadUrl(String reference, Duration expiry) {
        try {
            String url = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(StorageReferenceCodec.objectName(reference))
                    .expiry(Math.toIntExact(expiry.toSeconds()))
                    .build());
            return publicUri(URI.create(url));
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, "Unable to create download URL");
        }
    }

    private URI publicUri(URI signedUri) {
        try {
            return new URI(
                    publicEndpoint.getScheme(),
                    publicEndpoint.getUserInfo(),
                    publicEndpoint.getHost(),
                    publicEndpoint.getPort(),
                    signedUri.getRawPath(),
                    signedUri.getRawQuery(),
                    signedUri.getRawFragment());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, "Invalid public MinIO endpoint");
        }
    }

    @Override
    public void delete(String reference) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(StorageReferenceCodec.objectName(reference))
                    .build());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }

    private StoredObject store(boolean publicObject, String key, String contentType,
                               InputStream inputStream, long size) {
        String reference = StorageReferenceCodec.reference(publicObject, key);
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(StorageReferenceCodec.objectName(reference))
                    .contentType(contentType)
                    .stream(inputStream, size, -1)
                    .build());
            return new StoredObject(reference, publicObject ? StorageReferenceCodec.publicUrl(reference) : null);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.FILE_WRITE_FAILED, ex.getMessage());
        }
    }
}
