package com.furnisight.user.application.favorite.service;

import com.furnisight.user.application.favorite.dto.CatalogFavoriteProductSummary;
import com.furnisight.user.application.favorite.dto.GetFavoriteProductsQuery;
import com.furnisight.user.application.favorite.dto.FavoriteProductResponse;
import com.furnisight.user.application.favorite.port.in.usecase.GetFavoriteProductsUseCase;
import com.furnisight.user.application.favorite.port.out.CatalogFavoriteProductService;
import com.furnisight.user.domain.entities.favorite.FavoriteProduct;
import com.furnisight.user.domain.repository.favorite.FavoriteProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class GetFavoriteProductsService implements GetFavoriteProductsUseCase {
    private final FavoriteProductRepository favoriteProductRepository;
    private final CatalogFavoriteProductService catalogFavoriteProductService;

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteProductResponse> execute(GetFavoriteProductsQuery query) {
        List<FavoriteProduct> favorites = favoriteProductRepository.findAllByAccountId(query.accountId());
        Map<UUID, CatalogFavoriteProductSummary> products = catalogFavoriteProductService.getFavoriteProductSummaries(
            favorites.stream()
                .map(FavoriteProduct::getProductId)
                .toList(),
            query.locale()
        );

        return favorites.stream()
            .map(favorite -> FavoriteProductResponse.from(favorite, products.get(favorite.getProductId())))
            .toList();
    }
}
