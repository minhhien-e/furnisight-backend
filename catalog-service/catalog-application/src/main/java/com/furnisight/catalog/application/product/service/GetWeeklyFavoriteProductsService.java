package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.projection.ProductSummaryProjection;
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

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryProjection> execute(int limit) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return repository.findTopFavoritedProductsSince(oneWeekAgo, limit);
    }
}
