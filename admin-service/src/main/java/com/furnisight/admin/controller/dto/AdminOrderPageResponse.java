package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminOrderPageResponse(
        List<AdminOrderResponse> items,
        int totalPages,
        long totalElements,
        int currentPage
) {
}
