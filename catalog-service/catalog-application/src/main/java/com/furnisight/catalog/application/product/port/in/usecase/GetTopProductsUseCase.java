package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import java.util.List;

public interface GetTopProductsUseCase {
    List<ProductResponse> execute(int limit, String lang);
}
