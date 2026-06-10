package com.furnisight.admin.audit.web.dto.response;

import java.util.List;

public record AuditLogPageResponse(
        List<AuditLogResponse> items,
        long total,
        int page,
        int pageSize,
        int totalPages
) {
}
