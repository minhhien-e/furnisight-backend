package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.usecase.GetTopSellingProductsUseCase;
import com.furnisight.order.application.order.port.in.query.GetTopSellingProductsQuery;
import com.furnisight.order.domain.repository.order.TopSellingProductQuery;
import com.furnisight.order.domain.repository.order.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTopSellingProductsService implements GetTopSellingProductsUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingProductQuery> getTopSellingProducts(GetTopSellingProductsQuery query) {
        int limit = query.getLimit() > 0 ? query.getLimit() : 5;
        return orderRepository.findTopSellingProducts(limit);
    }

}
