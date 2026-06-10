package com.furnisight.admin.account.user.web.dto.response;

import com.furnisight.admin.account.role.web.dto.response.RoleResponse;

import java.util.List;

public record UserDetailResponse(
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
        List<RoleResponse> roles
) {
}
