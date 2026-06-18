package com.furnisight.cart.repository;

import com.furnisight.cart.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends MongoRepository<Cart, UUID> {
    List<Cart> findByItemsProductId(String productId);
}
