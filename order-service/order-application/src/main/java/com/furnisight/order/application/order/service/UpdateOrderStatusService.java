package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {
    private final OrderRepository orderRepository;

    @Override
    public void shipOrder(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));
        order.shipOrder();
        orderRepository.save(order);
    }

    @Override
    public void deliverOrder(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));
        order.deliverOrder();
        orderRepository.save(order);
    }

    @Override
    public void cancelOrder(String orderCode, java.util.UUID userId) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new ValidationException(ErrorCode.ORDER_NOT_FOUND);
        }

        order.cancelOrder();
        orderRepository.save(order);
    }
}
