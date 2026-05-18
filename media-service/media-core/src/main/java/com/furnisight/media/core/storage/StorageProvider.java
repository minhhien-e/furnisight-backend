package com.furnisight.media.core.storage;

import com.furnisight.media.core.model.enums.HttpMethod;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

public interface StorageProvider {

    /**
     * Upload object lên storage
     */
    void upload(
        String key,
        InputStream data,
        long contentLength,
        Map<String, String> metadata
    );

    /**
     * Download object từ storage
     */
    InputStream download(String key);

    /**
     * Xoá object
     */
    void delete(String key);

    /**
     * Kiểm tra object có tồn tại không
     */
    boolean exists(String key);

    /**
     * Sinh signed URL (upload / download)
     */
    String generatePresignedUrl(
        String key,
        HttpMethod method,
        Duration expiresIn
    );
}

