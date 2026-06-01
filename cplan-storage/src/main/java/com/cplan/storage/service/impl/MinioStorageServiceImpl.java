package com.cplan.storage.service.impl;

import com.cplan.common.result.Result;
import com.cplan.common.result.ResultCode;
import com.cplan.storage.config.MinioConfig;
import com.cplan.storage.service.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MinIO-backed implementation of StorageService.
 */
@Service
public class MinioStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageServiceImpl.class);

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public MinioStorageServiceImpl(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    /**
     * Initialize required buckets on startup.
     */
    @PostConstruct
    public void initBuckets() {
        String[] buckets = {
                minioConfig.getSegmentsBucket(),
                minioConfig.getFinalsBucket(),
                minioConfig.getAvatarsBucket(),
                minioConfig.getTempBucket()
        };

        for (String bucket : buckets) {
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucket).build()
                );
                if (!exists) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucket).build()
                    );
                    log.info("Created MinIO bucket: {}", bucket);
                } else {
                    log.debug("MinIO bucket already exists: {}", bucket);
                }
            } catch (Exception e) {
                log.warn("Failed to initialize bucket {}: {}", bucket, e.getMessage());
            }
        }
    }

    @Override
    public Result<Map<String, Object>> generateUploadUrl(String bucket, String objectKey, String contentType) {
        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(minioConfig.getPresignedExpirySeconds(), TimeUnit.SECONDS)
                            .build()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("uploadUrl", uploadUrl);
            data.put("expiresIn", minioConfig.getPresignedExpirySeconds());
            data.put("objectKey", objectKey);

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Failed to generate upload URL: bucket={}, key={}, error={}",
                    bucket, objectKey, e.getMessage());
            return Result.fail(ResultCode.MINIO_UPLOAD_ERROR);
        }
    }

    @Override
    public Result<Map<String, Object>> generateDownloadUrl(String bucket, String objectKey) {
        try {
            String downloadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(minioConfig.getPresignedExpirySeconds(), TimeUnit.SECONDS)
                            .build()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("downloadUrl", downloadUrl);
            data.put("expiresIn", minioConfig.getPresignedExpirySeconds());

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Failed to generate download URL: bucket={}, key={}, error={}",
                    bucket, objectKey, e.getMessage());
            return Result.fail(ResultCode.MINIO_DOWNLOAD_ERROR);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            log.info("Deleted object: bucket={}, key={}", bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete object: bucket={}, key={}, error={}",
                    bucket, objectKey, e.getMessage());
            throw new RuntimeException("Failed to delete object from MinIO", e);
        }
    }
}
