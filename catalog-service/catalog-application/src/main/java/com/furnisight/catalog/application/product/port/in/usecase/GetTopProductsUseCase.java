package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import java.util.List;

public interface GetTopProductsUseCase {
    List<ProductDetailProjection> execute(int limit);
}
