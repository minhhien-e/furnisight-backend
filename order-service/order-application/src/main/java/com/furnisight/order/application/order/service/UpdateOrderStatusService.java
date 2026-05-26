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
    public void updateOrderStatus(UpdateOrderStatusCommand command) {
        Order order = orderRepository.findByOrderCode(command.getOrderCode())
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));
        
        try {
            com.furnisight.order.domain.enums.OrderStatus newStatus = com.furnisight.order.domain.enums.OrderStatus.valueOf(command.getStatus().toUpperCase());
            order.setStatus(newStatus);
            orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }
}
