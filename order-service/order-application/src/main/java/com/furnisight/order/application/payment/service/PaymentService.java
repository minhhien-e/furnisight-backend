package com.furnisight.order.application.payment.service;

import com.furnisight.order.application.payment.port.in.command.CreatePaymentCommand;
import com.furnisight.order.application.payment.port.in.usecase.CreatePaymentUseCase;
import com.furnisight.order.application.payment.port.in.usecase.ProcessPaymentCallbackUseCase;
import com.furnisight.order.application.payment.port.out.PaymentGatewayPort;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.application.order.port.out.event.InventoryEventPublisherPort;
import com.furnisight.order.domain.entities.reservation.StockReservation;
import com.furnisight.order.domain.repository.reservation.StockReservationRepository;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService implements CreatePaymentUseCase, ProcessPaymentCallbackUseCase {

    private final OrderRepository orderRepository;
    private final java.util.List<PaymentGatewayPort> gateways;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryEventPublisherPort inventoryEventPublisher;

    private PaymentGatewayPort getGateway(String paymentMethod) {
        return gateways.stream()
                .filter(g -> g.getPaymentMethod().equalsIgnoreCase(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_PAYMENT_METHOD));
    }

    @Override
    @Transactional
    public String createPaymentUrl(CreatePaymentCommand command) {
        PaymentGatewayPort gateway = getGateway(command.getPaymentMethod());

        Order order = orderRepository.findByOrderCode(command.getOrderCode())
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));

        order.markPaymentInitiated(LocalDateTime.now());
        orderRepository.save(order);

        return gateway.generatePaymentUrl(order, command.getClientIp());
    }

    @Override
    @Transactional
    public boolean processCallback(String paymentMethod, Map<String, String> callbackParams) {
        PaymentGatewayPort gateway = getGateway(paymentMethod);
        com.furnisight.order.application.payment.port.out.PaymentCallbackResult result = gateway.processCallback(callbackParams);

        Order order = orderRepository.findByOrderCode(result.getOrderCode())
                .orElseThrow(() -> new ValidationException(ErrorCode.ORDER_NOT_FOUND));

        if (!result.isSuccess()) {
            order.markPaymentFailed(paymentMethod.toUpperCase(), LocalDateTime.now(), result.getErrorMessage());
            orderRepository.save(order);
            
            // Release stock
            releaseStock(result.getOrderCode());
            return false;
        }

        order.markAsPaid(paymentMethod.toUpperCase(), result.getAmount(), result.getPaidAt());
        orderRepository.save(order);
        
        // Delete stock reservation since it's paid
        stockReservationRepository.deleteByOrderCode(result.getOrderCode());
        return true;
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
