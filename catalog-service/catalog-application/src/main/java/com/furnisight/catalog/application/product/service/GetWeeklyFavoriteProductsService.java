package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.response.ProductResponse;
import com.furnisight.catalog.application.product.port.in.usecase.GetWeeklyFavoriteProductsUseCase;
import com.furnisight.catalog.application.product.port.out.FavoriteProductReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetWeeklyFavoriteProductsService implements GetWeeklyFavoriteProductsUseCase {
    private final FavoriteProductReadRepository repository;
    private final ProductTranslationService productTranslationService;
    private final ProductReviewStatsEnricher productReviewStatsEnricher;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> execute(int limit, String lang) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<ProductResponse> products = repository.findTopFavoritedProductsSince(oneWeekAgo, limit);
        productReviewStatsEnricher.enrichAll(products);
        return productTranslationService.localizeProducts(
                products,
                lang);
    }
}
