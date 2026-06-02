package com.furnisight.admin.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserProfileAdminClient {

    private final RestClient restClient;

    public UserProfileAdminClient(@Value("${services.user.base-url:http://localhost:8080/api/v1}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void updateDisplayName(UUID accountId, String displayName) {
        NameParts nameParts = splitName(displayName);
        UpdateProfileRequest request = new UpdateProfileRequest(
                clean(displayName),
                nameParts.firstName(),
                nameParts.lastName(),
                null,
                null,
                null,
                null);

        restClient.put()
                .uri("/profile")
                .header("X-User-Id", accountId.toString())
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private NameParts splitName(String rawName) {
        String name = clean(rawName);
        if (name.isBlank()) {
            return new NameParts("", "");
        }
        String[] parts = name.split("\\s+", 2);
        if (parts.length == 1) {
            return new NameParts(parts[0], "");
        }
        return new NameParts(parts[0], parts[1]);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private record NameParts(String firstName, String lastName) {
    }

    private record UpdateProfileRequest(
            String displayName,
            String firstName,
            String lastName,
            UUID avatarMediaId,
            String bio,
            LocalDate birthday,
            String gender) {
    }
}
