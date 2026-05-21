package com.furnisight.catalog.domain.valueobjects.review;

import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Embeddable
public record ReviewContent(String text, String hash) {

    public static ReviewContent from(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException(ErrorCode.INVALID_CONTENT, Map.of("reason", "Text empty"));
        }
        return new ReviewContent(text, generateHash(text));
    }

    private static String generateHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(text.getBytes());
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
