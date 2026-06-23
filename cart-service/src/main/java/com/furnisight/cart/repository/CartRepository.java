package com.furnisight.cart.repository;

import com.furnisight.cart.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends MongoRepository<Cart, UUID> {
    List<Cart> findByItemsProductId(String productId);

    @Query(value = "{ 'items.0': { $exists: true }, 'updatedAt': { $lte: ?0 } }", fields = "{ '_id': 1 }")
    List<Cart> findAbandonedCartIds(LocalDateTime updatedBefore);
}
