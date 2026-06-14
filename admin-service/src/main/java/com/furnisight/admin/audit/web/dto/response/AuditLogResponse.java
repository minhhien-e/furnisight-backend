package com.furnisight.admin.audit.web.dto.response;

public record AuditLogResponse(
        String id,
        String actorId,
        String actorName,
        String actionType,
        String action,
        String resourceType,
        String resourceId,
        String result,
        String detail,
        String time,
        String meta,
        String tone,
        String status,
        String ipAddress,
        String userAgent,
        String createdAt
) {
}
