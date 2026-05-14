package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.projection.SearchProductsProjection;
import com.furnisight.catalog.application.product.port.in.usecase.SearchProductsUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchProductsService implements SearchProductsUseCase {

    private final ProductReadRepository productReadRepository;

    @Override
    public SearchProductsProjection execute(String query, UUID categoryId, String status, int page, int size) {
        return productReadRepository.searchProducts(query, categoryId, status, page, size);
    }
}
