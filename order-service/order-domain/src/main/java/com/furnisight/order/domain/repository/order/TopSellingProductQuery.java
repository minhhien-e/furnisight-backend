package com.furnisight.order.domain.repository.order;

public record TopSellingProductQuery(
        String productId,
        String productName,
        String categoryName,
        String imageUrl,
        Double price,
        Integer soldCount,
        Double totalRevenue
) {
}
