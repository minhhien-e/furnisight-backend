package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.services.OrderLifecycle;
import com.furnisight.order.domain.valueobjects.order.ShippingDetail;
import com.furnisight.order.domain.valueobjects.order.PaymentDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderLifecycle orderLifecycle;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        // Map Application Command to Domain Parameter
        var itemParams = command.getItems() == null ? null : command.getItems().stream()
                .map(item -> OrderLifecycle.OrderItemParam.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .categoryName(item.getCategoryName())
                        .productName(item.getProductName())
                        .variantDescription(item.getVariantDescription())
                        .price(item.getPrice())
                        .oldPrice(item.getOldPrice())
                        .quantity(item.getQuantity())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        var shippingDetail = ShippingDetail.builder()
                .shippingAddressName(command.getShippingAddressName())
                .shippingAddressPhone(command.getShippingAddressPhone())
                .shippingAddressDetail(command.getShippingAddressDetail())
                .shippingMethod(command.getShippingMethod())
                .build();

        var paymentDetail = PaymentDetail.builder()
                .paymentMethod(command.getPaymentMethod())
                .paymentStatus("PENDING")
                .paidAmount(0.0)
                .build();

        // Delegate core business logic to Domain Service
        Order order = orderLifecycle.createPendingOrder(
                command.getUserId(),
                command.getCustomerNote(),
                shippingDetail,
                paymentDetail,
                itemParams
        );

        Order savedOrder = orderRepository.save(order);
        return savedOrder.getId();
    }
}
