package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.usecase.GetOrderQuery;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrderService implements GetOrderQuery {
    private final OrderRepository orderRepository;

    @Override
    public List<Order> getUserOrders(UUID userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public Order getOrderDetail(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    public List<Order> getAdminOrders(String status) {
        if (status != null && !status.trim().isEmpty()) {
            try {
                com.furnisight.order.domain.enums.OrderStatus orderStatus = com.furnisight.order.domain.enums.OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findAllByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
            }
        }
        return orderRepository.findAll();
    }

    @Override
    public Optional<UUID> getDeliveredOrderItemIdForProduct(UUID userId, String productId) {
        return orderRepository.findDeliveredOrderItemIdByUserIdAndProductId(userId, productId);
    }
}
