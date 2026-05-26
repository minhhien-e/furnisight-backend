package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.UpdateOrderStatusCommand;
import com.furnisight.order.application.order.port.in.usecase.UpdateOrderStatusUseCase;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.application.order.port.out.event.InventoryEventPublisherPort;
import com.furnisight.order.domain.entities.reservation.StockReservation;
import com.furnisight.order.domain.repository.reservation.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {
    private final OrderRepository orderRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryEventPublisherPort inventoryEventPublisher;

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
        
        releaseStock(orderCode);
    }
    
    private void releaseStock(String orderCode) {
        java.util.List<StockReservation> reservations = stockReservationRepository.findByOrderCode(orderCode);
        if (reservations != null && !reservations.isEmpty()) {
            java.util.List<InventoryEventPublisherPort.StockItem> stockItems = new java.util.ArrayList<>();
            for (StockReservation res : reservations) {
                stockItems.add(InventoryEventPublisherPort.StockItem.builder()
                        .productId(res.getProductId())
                        .variantId(res.getProductVariantId())
                        .quantity(res.getQuantity())
                        .build());
            }
            inventoryEventPublisher.publishStockReleaseEvent(orderCode, stockItems);
            stockReservationRepository.deleteByOrderCode(orderCode);
        }
    }
}
