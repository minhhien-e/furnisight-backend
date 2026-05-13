package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import com.furnisight.catalog.domain.enums.product.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UpdateProductStatusRequest {
    private UUID shopId;
    private ProductStatus status;
}
