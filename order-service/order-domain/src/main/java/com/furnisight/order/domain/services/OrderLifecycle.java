package com.furnisight.order.domain.services;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.enums.OrderStatus;
import com.furnisight.order.domain.services.dto.OrderItemParam;
import com.furnisight.order.domain.valueobjects.ProductSnapshot;
import com.furnisight.order.domain.valueobjects.OrderFee;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import com.furnisight.order.domain.valueobjects.ShippingDetail;
import com.furnisight.order.domain.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLifecycle {

    private final OrderCodeGenerator orderCodeGenerator;

    public Order createPendingOrder(
            UUID userId,
            String customerNote,
            ShippingDetail shippingDetail,
            PaymentDetail paymentDetail,
            List<OrderItemParam> itemParams) {

        if (itemParams == null || itemParams.isEmpty()) {
            throw new ValidationException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        // Assume default fees
        double shippingFee = 15000.0;
        double shippingDiscount = 0.0;
        double discountAmount = 0.0;
        double insuranceFee = 0.0;

        var items = itemParams.stream().map(param ->
                OrderItem.builder()
                        .id(UUID.randomUUID())
                        .productSnapshot(ProductSnapshot.builder()
                                .productId(param.getProductId())
                                .variantId(param.getVariantId())
                                .categoryName(param.getCategoryName())
                                .productName(param.getProductName())
                                .color(param.getColor())
                                .material(param.getMaterial())
                                .warranty(param.getWarranty())
                                .dimensions(param.getDimensions())
                                .imageUrl(param.getImageUrl())
                                .build())
                        .price(param.getPrice())
                        .oldPrice(param.getOldPrice())
                        .quantity(param.getQuantity())
                        .build()
        ).collect(Collectors.toList());

        String orderCode = orderCodeGenerator.generate();

        return Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode(orderCode)
                .status(OrderStatus.UNPAID)
                .fee(OrderFee.builder()
                        .shippingFee(shippingFee)
                        .shippingDiscount(shippingDiscount)
                        .discountAmount(discountAmount)
                        .insuranceFee(insuranceFee)
                        .build())
                .customerNote(customerNote)
                .shippingDetail(shippingDetail)
                .paymentDetail(paymentDetail)
                .items(items)
                .build();
    }
}
