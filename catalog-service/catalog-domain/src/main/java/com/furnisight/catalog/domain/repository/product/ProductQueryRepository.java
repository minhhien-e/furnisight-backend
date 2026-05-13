package com.furnisight.catalog.domain.repository.product;

import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;

import java.util.Optional;
import java.util.UUID;

public interface ProductQueryRepository {
    Optional<ProductDetailResponseDto> findProductDetailById(UUID productId);
}
