package com.furnisight.media.infrastructure.storage.local;

import com.furnisight.media.core.exception.MediaNotFoundException;
import com.furnisight.media.core.exception.StorageException;
import com.furnisight.media.core.exception.enums.ErrorCode;
import com.furnisight.media.core.model.enums.HttpMethod;
import com.furnisight.media.infrastructure.signer.HmacSigner;
import com.furnisight.media.core.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LocalStorageProvider implements StorageProvider {
    private final LocalStorageProperties properties;
    private final HmacSigner singer;

    @Override
    public void upload(String key, InputStream data, long contentLength, Map<String, String> metadata) {
        // 1️⃣ Resolve path từ key
        Path filePath = resolve(key);
        try {
            // Tạo folder
            Files.createDirectories(filePath.getParent());

            // Ghi dữ liệu vào file (ghi đè nếu file đã tồn tại)
            Files.copy(data, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException(ErrorCode.STORAGE_ERROR, "Failed to upload file: " + key, Map.of("key", key));
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            // 1️⃣ Resolve path từ key
            Path filePath = resolve(key);

            // 2️⃣ Check tồn tại & là file
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new MediaNotFoundException(key);
            }

            // 3️⃣ FileInputStream
            FileInputStream fis = new FileInputStream(filePath.toFile());

            // 4️⃣ Bọc BufferedInputStream
            return new BufferedInputStream(fis);

        } catch (Exception e) {
            throw new StorageException(ErrorCode.STORAGE_ERROR, "Failed to download file: " + key, Map.of("key", key));
        }
    }

    @Override
    public void delete(String key) {
        try {
            // 1️⃣ Resolve path từ key
            Path filePath = resolve(key);

            // 2️⃣ Check tồn tại & là file
            if (!Files.exists(filePath)) {
                throw new MediaNotFoundException(key);
            }

            // Xóa file
            Files.delete(filePath);
        } catch (Exception e) {
            throw new StorageException(ErrorCode.STORAGE_ERROR, "Failed to delete file: " + key, Map.of("key", key));
        }
    }

    @Override
    public boolean exists(String key) {
        // 1️⃣ Resolve path từ key
        Path filePath = resolve(key);
        return Files.exists(filePath);
    }

    @Override
    public String generatePresignedUrl(
        String key,
        HttpMethod method,
        Duration expiresIn
    ) {

        // 1️⃣ Tính thời điểm hết hạn (epoch seconds)
        long expiresAt = Instant.now()
            .plus(expiresIn)
            .getEpochSecond();

        // 2️⃣ Build dữ liệu cần ký (CANONICAL DATA)
        String dataToSign =
            method.name() + "\n" +
                key + "\n" +
                expiresAt;

        // 3️⃣ Ký dữ liệu bằng secret (HMAC)
        String signature = singer.sign(dataToSign);

        // 4️⃣ Chọn endpoint theo method
        String path;
        if (method == HttpMethod.GET) {
            path = "/media/download/";
        } else if (method == HttpMethod.PUT) {
            path = "/media/upload/";
        } else {
            throw new UnsupportedOperationException("Unsupported method: " + method);
        }

        return String.format(
            "%s%s%s?expires=%d&signature=%s",
            properties.getBaseUrl(),
            path,
            key,
            expiresAt,
            signature
        );
    }

    private Path resolve(String key) {
        return properties.getDirectory().resolve(key).normalize();
    }
}
