package com.cplan.storage.controller;

import com.cplan.common.result.Result;
import com.cplan.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Storage REST controller — presigned URL generation for uploads and downloads.
 */
@RestController
@RequestMapping("/api/storage/v1")
public class StorageController {

    private static final Logger log = LoggerFactory.getLogger(StorageController.class);

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Generate a pre-signed upload URL.
     * POST /api/storage/v1/upload/presign
     */
    @PostMapping("/upload/presign")
    public Result<Map<String, Object>> generateUploadUrl(@RequestBody Map<String, String> request) {
        String bucket = request.getOrDefault("bucket", "cplan-segments");
        String objectKey = request.get("objectKey");
        String contentType = request.getOrDefault("contentType", "application/octet-stream");

        if (objectKey == null || objectKey.isBlank()) {
            return Result.fail(400, "objectKey is required");
        }

        log.info("Generate upload URL: bucket={}, key={}", bucket, objectKey);
        return storageService.generateUploadUrl(bucket, objectKey, contentType);
    }

    /**
     * Generate a pre-signed download URL.
     * POST /api/storage/v1/download/presign
     */
    @PostMapping("/download/presign")
    public Result<Map<String, Object>> generateDownloadUrl(@RequestBody Map<String, String> request) {
        String bucket = request.getOrDefault("bucket", "cplan-segments");
        String objectKey = request.get("objectKey");

        if (objectKey == null || objectKey.isBlank()) {
            return Result.fail(400, "objectKey is required");
        }

        log.info("Generate download URL: bucket={}, key={}", bucket, objectKey);
        return storageService.generateDownloadUrl(bucket, objectKey);
    }
}
