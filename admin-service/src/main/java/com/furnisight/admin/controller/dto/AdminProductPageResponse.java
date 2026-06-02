package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminProductPageResponse(
        List<AdminProductResponse> items,
        int totalPages,
        long totalElements,
        int currentPage
) {
}
