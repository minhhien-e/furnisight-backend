package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.projection.ProductDetailProjection;
import com.furnisight.catalog.application.product.port.in.usecase.GetTopProductsUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopProductsService implements GetTopProductsUseCase {

    private final ProductReadRepository productReadRepository;

    @Override
    public List<ProductDetailProjection> execute(int limit) {
        return productReadRepository.findTopProducts(limit);
    }
}
