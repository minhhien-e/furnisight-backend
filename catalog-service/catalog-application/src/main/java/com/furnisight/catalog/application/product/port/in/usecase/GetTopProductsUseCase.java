package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.projection.ProductSummaryProjection;
import java.util.List;

public interface GetTopProductsUseCase {
    List<ProductSummaryProjection> execute(int limit);
}
