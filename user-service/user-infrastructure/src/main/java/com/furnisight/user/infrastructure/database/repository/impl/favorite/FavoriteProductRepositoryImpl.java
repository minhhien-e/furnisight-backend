package com.furnisight.user.infrastructure.database.repository.impl.favorite;

import com.furnisight.user.domain.entities.favorite.FavoriteProduct;
import com.furnisight.user.domain.repository.favorite.FavoriteProductRepository;
import com.furnisight.user.infrastructure.database.repository.jpa.favorite.FavoriteProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FavoriteProductRepositoryImpl implements FavoriteProductRepository {
    private final FavoriteProductJpaRepository favoriteProductJpaRepository;

    @Override
    public FavoriteProduct save(FavoriteProduct favoriteProduct) {
        return favoriteProductJpaRepository.save(favoriteProduct);
    }

    @Override
    public Optional<FavoriteProduct> findByAccountIdAndProductId(UUID accountId, UUID productId) {
        return favoriteProductJpaRepository.findByAccountIdAndProductId(accountId, productId);
    }

    @Override
    public void delete(FavoriteProduct favoriteProduct) {
        favoriteProductJpaRepository.delete(favoriteProduct);
    }

    @Override
    public List<FavoriteProduct> findAllByAccountId(UUID accountId) {
        return favoriteProductJpaRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
