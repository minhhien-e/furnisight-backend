package com.furnisight.admin.marketing.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MarketingComboRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        UUID imageMediaId,
        String imageUrl,
        @NotBlank(message = "Discount type is required") String discountType,
        @NotNull(message = "Discount value is required") @Min(0) Double discountValue,
        @NotNull(message = "Start date is required") LocalDateTime startDate,
        @NotNull(message = "End date is required") LocalDateTime endDate,
        Boolean active,
        @NotEmpty(message = "Items cannot be empty") @Valid List<Item> items
) {
    public record Item(
            @NotBlank(message = "Product ID is required") String productId,
            String variantId,
            @NotNull(message = "Quantity is required") @Min(1) Integer quantity,
            @NotBlank(message = "Product name is required") String productName,
            String sku,
            String categoryName,
            String image,
            @NotNull(message = "Price is required") @Min(0) Double price
    ) {
    }
}
