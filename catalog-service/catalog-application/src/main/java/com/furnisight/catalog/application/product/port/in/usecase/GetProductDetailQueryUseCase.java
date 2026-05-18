package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;

public interface GetProductDetailQueryUseCase {
    ProductDetailProjection execute(GetProductDetailQuery query);
}
