package com.furnisight.user.domain.entities.favorite;

import com.furnisight.user.domain.events.favorite.ProductFavoritedEvent;
import com.furnisight.user.domain.seedwork.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "favorite_products",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_favorite_products_account_product",
        columnNames = {"account_id", "product_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteProduct extends AggregateRoot {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    public FavoriteProduct(UUID accountId, UUID productId) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.productId = productId;
        registerEvent(new ProductFavoritedEvent(id, accountId, productId, LocalDateTime.now()));
    }
}
