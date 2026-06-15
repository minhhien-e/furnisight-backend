package com.furnisight.order.domain.repository.order;

import com.furnisight.order.domain.entities.order.OrderStatusHistory;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository {
    OrderStatusHistory save(OrderStatusHistory history);
    List<OrderStatusHistory> findByOrderId(UUID orderId);
}
