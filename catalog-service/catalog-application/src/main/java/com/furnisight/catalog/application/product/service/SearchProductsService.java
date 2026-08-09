package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.common.dto.PageResponse;
import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.in.usecase.SearchProductsUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import com.furnisight.catalog.application.product.dto.query.SearchProductsQuery;

@Service
@RequiredArgsConstructor
public class SearchProductsService implements SearchProductsUseCase {

    private final ProductReadRepository productReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "search_products", sync = true)
    public PageResponse<ProductResponse> execute(SearchProductsQuery query) {
        if (query.getQ() != null && !query.getQ().isBlank()) {
            query.setQ(productTranslationService.translateSearchQuery(query.getQ(), query.getLang()));
        }
        PageResponse<ProductResponse> page = productReadRepository.searchProducts(query);
        return productTranslationService.localizePage(page, query.getLang());
    }
}
