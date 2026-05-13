package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;
import java.util.List;
import java.util.UUID;

public interface SearchProductsUseCase {
    List<ProductDetailResponseDto> execute(String query, UUID categoryId, String status, int page, int size);
}
