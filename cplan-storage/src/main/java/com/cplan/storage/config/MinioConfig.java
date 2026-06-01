package com.cplan.storage.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO client configuration.
 * Connection details are loaded from Nacos Config (cplan-storage.yaml).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cplan.minio")
public class MinioConfig {

    /** MinIO API endpoint. */
    private String endpoint = "http://127.0.0.1:9000";

    /** Access key (username). */
    private String accessKey = "cplan-minio";

    /** Secret key (password). */
    private String secretKey = "cplan@minio123";

    /** Default bucket for video segments. */
    private String segmentsBucket = "cplan-segments";

    /** Default bucket for final composed videos. */
    private String finalsBucket = "cplan-finals";

    /** Default bucket for user avatars. */
    private String avatarsBucket = "cplan-avatars";

    /** Bucket for temporary files. */
    private String tempBucket = "cplan-temp";

    /** Pre-signed URL expiry in seconds (default: 1 hour). */
    private int presignedExpirySeconds = 3600;

    /**
     * Create MinIO client bean.
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
