package com.furnisight.admin.account.user.web.dto.response;

import com.furnisight.admin.account.role.web.dto.response.RoleResponse;

import java.util.List;

public record UserSummaryResponse(
        String id,
        String name,
        String email,
        String status,
        String statusLabel,
        String role,
        List<RoleResponse> roles,
        String phone,
        String createdAt,
        String avTone,
        String av
) {
}
