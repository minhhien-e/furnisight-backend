package com.furnisight.catalog.application.product.port.in.usecase;

import com.furnisight.catalog.application.product.dto.query.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;

public interface GetProductDetailQueryUseCase {
    ProductResponse execute(GetProductDetailQuery query);
}
