package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.in.usecase.GetTopProductsUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopProductsService implements GetTopProductsUseCase {

    private final ProductReadRepository productReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    @Cacheable(value = "top_products", sync = true)
    public List<ProductResponse> execute(int limit, String lang) {
        List<ProductResponse> products = productReadRepository.findTopProducts(limit);
        return productTranslationService.localizeProducts(
                products,
                lang);
    }
}
