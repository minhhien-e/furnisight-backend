package com.furnisight.user.application.favorite.service;

import com.furnisight.user.application.favorite.dto.FavoriteProductCommand;
import com.furnisight.user.application.favorite.dto.FavoriteProductResponse;
import com.furnisight.user.application.favorite.port.in.usecase.FavoriteProductUseCase;
import com.furnisight.user.application.favorite.port.out.CatalogFavoriteProductService;
import com.furnisight.user.domain.entities.favorite.FavoriteProduct;
import com.furnisight.user.domain.repository.favorite.FavoriteProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteProductService implements FavoriteProductUseCase {
    private final FavoriteProductRepository favoriteProductRepository;
    private final CatalogFavoriteProductService catalogFavoriteProductService;

    @Override
    @Transactional
    public FavoriteProductResponse execute(FavoriteProductCommand command) {
        FavoriteProduct favorite = favoriteProductRepository
            .findByAccountIdAndProductId(command.accountId(), command.productId())
            .orElseGet(() -> favoriteProductRepository.save(
                new FavoriteProduct(command.accountId(), command.productId())
            ));

        var products = catalogFavoriteProductService.getFavoriteProductSummaries(
                List.of(favorite.getProductId()),
                command.locale()
        );
        return FavoriteProductResponse.from(favorite, products.get(favorite.getProductId()));
    }
}
