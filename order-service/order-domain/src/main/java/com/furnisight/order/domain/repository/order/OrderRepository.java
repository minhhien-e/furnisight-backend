package com.furnisight.order.domain.repository.order;

import com.furnisight.order.domain.entities.order.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
}
