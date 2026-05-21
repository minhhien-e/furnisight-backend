package com.furnisight.media.infrastructure.utils;

import java.io.InputStream;
import java.security.MessageDigest;

public final class FileHashUtil {

    private static final int BUFFER_SIZE = 8192;

    private FileHashUtil() {}

    public static String sha256(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BUFFER_SIZE];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            return toHex(hashBytes);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate SHA-256 hash", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
