package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminAccountDetailResponse(
        String id,
        String email,
        String username,
        String status,
        String statusLabel,
        String createdAt,
        String firstName,
        String lastName,
        String name,
        String avatarUrl,
        List<AdminRoleResponse> roles
) {
}
