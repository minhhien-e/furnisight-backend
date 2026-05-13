package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;

public interface GetProductDetailQueryUseCase {
    ProductDetailResponseDto execute(GetProductDetailQuery query);
}
