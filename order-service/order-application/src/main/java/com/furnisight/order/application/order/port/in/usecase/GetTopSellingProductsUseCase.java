package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.application.order.port.in.query.GetTopSellingProductsQuery;
import com.furnisight.order.domain.repository.order.TopSellingProductQuery;
import java.util.List;

public interface GetTopSellingProductsUseCase {
    List<TopSellingProductQuery> getTopSellingProducts(GetTopSellingProductsQuery query);
}
