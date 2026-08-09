package com.furnisight.order.application.order.port.in.usecase;

import com.furnisight.order.domain.entities.order.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetOrderQuery {
    List<Order> getUserOrders(UUID userId);
    Order getOrderDetail(String orderCode);
    List<Order> getAdminOrders(String status);
    Optional<UUID> getDeliveredOrderItemIdForProduct(UUID userId, String productId);
}
