package com.furnisight.admin.order.web.dto.response;

import java.util.List;

public record OrderPageResponse(
        List<OrderResponse> items,
        int totalPages,
        long totalElements,
        int currentPage
) {
}
