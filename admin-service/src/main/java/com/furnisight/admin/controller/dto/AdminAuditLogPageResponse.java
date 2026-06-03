package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminAuditLogPageResponse(
        List<AdminAuditLogResponse> items,
        long total,
        int page,
        int pageSize,
        int totalPages
) {
}
