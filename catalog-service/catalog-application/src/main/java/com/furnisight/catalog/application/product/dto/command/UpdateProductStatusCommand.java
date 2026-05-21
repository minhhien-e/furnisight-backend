package com.furnisight.catalog.application.product.dto.command;

import com.furnisight.catalog.domain.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductStatusCommand {
    private UUID productId;
    private ProductStatus status;
}
