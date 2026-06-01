package com.cplan.storage.service;

import com.cplan.common.result.Result;

import java.util.Map;

/**
 * Storage service interface for MinIO operations.
 */
public interface StorageService {

    /**
     * Generate a pre-signed upload URL.
     *
     * @param bucket      MinIO bucket name
     * @param objectKey   object key within the bucket
     * @param contentType MIME type of the file
     * @return map containing uploadUrl, expiresIn, and objectKey
     */
    Result<Map<String, Object>> generateUploadUrl(String bucket, String objectKey, String contentType);

    /**
     * Generate a pre-signed download URL.
     *
     * @param bucket    MinIO bucket name
     * @param objectKey object key within the bucket
     * @return map containing downloadUrl and expiresIn
     */
    Result<Map<String, Object>> generateDownloadUrl(String bucket, String objectKey);

    /**
     * Delete an object from MinIO.
     *
     * @param bucket    MinIO bucket name
     * @param objectKey object key within the bucket
     */
    void deleteObject(String bucket, String objectKey);
}
