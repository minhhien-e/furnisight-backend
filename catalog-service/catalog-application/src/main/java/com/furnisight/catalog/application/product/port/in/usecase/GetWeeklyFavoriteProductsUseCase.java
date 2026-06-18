package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import java.util.List;

public interface GetWeeklyFavoriteProductsUseCase {
    List<ProductResponse> execute(int limit);
}
