package com.furnisight.catalog.domain.exceptions;

import com.furnisight.catalog.domain.exceptions.enums.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ProductOwnershipException extends BaseException {
    public ProductOwnershipException(UUID shopId, UUID productId) {
        super(
                ErrorCode.PRODUCT_OWNERSHIP_DENIED,
                "Action denied: Shop ID " + shopId + " không phải là chủ sở hữu của Product " + productId,
                Map.of("shopId", shopId, "productId", productId));
    }
}
