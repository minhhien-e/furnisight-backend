package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.in.usecase.GetTopProductsUseCase;
import com.furnisight.catalog.application.product.port.out.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopProductsService implements GetTopProductsUseCase {

    private final ProductReadRepository productReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    public List<ProductResponse> execute(int limit, String lang) {
        return productTranslationService.localizeProducts(
                productReadRepository.findTopProducts(limit),
                lang);
    }
}
