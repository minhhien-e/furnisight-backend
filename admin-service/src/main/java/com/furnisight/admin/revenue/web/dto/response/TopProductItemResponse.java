package com.furnisight.admin.revenue.web.dto.response;

public record TopProductItemResponse(
        String productId,
        String productName,
        String categoryName,
        String imageUrl,
        double price,
        int soldCount,
        double totalRevenue
) {}
