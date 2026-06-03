package com.furnisight.admin.controller.dto;

public record AdminAuditLogResponse(
        String id,
        String actorId,
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
