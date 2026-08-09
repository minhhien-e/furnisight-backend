package com.furnisight.user.infrastructure.database.repository.jpa.favorite;

import com.furnisight.user.domain.entities.favorite.FavoriteProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteProductJpaRepository extends JpaRepository<FavoriteProduct, UUID> {
    Optional<FavoriteProduct> findByAccountIdAndProductId(UUID accountId, UUID productId);

    List<FavoriteProduct> findAllByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
