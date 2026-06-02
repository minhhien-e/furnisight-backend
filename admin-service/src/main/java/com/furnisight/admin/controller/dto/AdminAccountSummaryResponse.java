package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminAccountSummaryResponse(
        String id,
        String name,
        String email,
        String status,
        String statusLabel,
        String role,
        List<AdminRoleResponse> roles,
        String phone,
        String createdAt,
        String avTone,
        String av
) {
}
