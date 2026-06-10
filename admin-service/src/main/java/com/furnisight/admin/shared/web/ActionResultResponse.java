package com.furnisight.admin.shared.web;

public record ActionResultResponse(
        boolean success,
        String message
) {
}
