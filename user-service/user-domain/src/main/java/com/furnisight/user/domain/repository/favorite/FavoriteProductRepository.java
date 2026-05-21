package com.furnisight.user.domain.repository.favorite;

import com.furnisight.user.domain.entities.favorite.FavoriteProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteProductRepository {
    FavoriteProduct save(FavoriteProduct favoriteProduct);

    Optional<FavoriteProduct> findByAccountIdAndProductId(UUID accountId, UUID productId);

    void delete(FavoriteProduct favoriteProduct);

    List<FavoriteProduct> findAllByAccountId(UUID accountId);
}
