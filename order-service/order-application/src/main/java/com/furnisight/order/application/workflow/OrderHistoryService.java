package com.furnisight.order.application.workflow;

import com.furnisight.order.application.processing.OrderProcessingContext;
import com.furnisight.order.domain.entities.order.OrderStatusHistory;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.repository.order.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {
    private final OrderStatusHistoryRepository repository;

    public void record(OrderProcessingContext context, OrderStatus previousStatus) {
        repository.save(OrderStatusHistory.builder()
                .orderId(context.getOrder().getId())
                .orderCode(context.getOrder().getOrderCode())
                .previousStatus(previousStatus)
                .nextStatus(context.getOrder().getStatus())
                .actorId(context.getActorId())
                .actorType(context.getActorType())
                .trackingCode(context.getOrder().getTrackingCode())
                .note(context.getNote())
                .build());
    }
}
