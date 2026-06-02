package com.furnisight.admin.controller.dto;

public record AdminActionResultResponse(
        boolean success,
        String message
) {
}
