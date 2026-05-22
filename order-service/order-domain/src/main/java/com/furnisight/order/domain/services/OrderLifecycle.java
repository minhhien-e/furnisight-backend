package com.furnisight.order.domain.services;

import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.entities.order.OrderItem;
import com.furnisight.order.domain.valueobjects.order.OrderFee;
import com.furnisight.order.domain.valueobjects.order.PaymentDetail;
import com.furnisight.order.domain.valueobjects.order.ShippingDetail;
import com.furnisight.order.domain.exceptions.DomainException;
import com.furnisight.order.domain.exceptions.order.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderLifecycle {

    public Order createPendingOrder(
            UUID userId,
            String customerNote,
            ShippingDetail shippingDetail,
            PaymentDetail paymentDetail,
            List<OrderItemParam> itemParams) {

        if (itemParams == null || itemParams.isEmpty()) {
            throw new com.furnisight.order.domain.exceptions.order.ValidationException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        // Assume default fees
        double shippingFee = 15000.0;
        double shippingDiscount = 0.0;
        double discountAmount = 0.0;
        double insuranceFee = 0.0;

        var items = itemParams.stream().map(param ->
                OrderItem.builder()
                        .id(UUID.randomUUID())
                        .productId(param.getProductId())
                        .variantId(param.getVariantId())
                        .categoryName(param.getCategoryName())
                        .productName(param.getProductName())
                        .variantDescription(param.getVariantDescription())
                        .price(param.getPrice())
                        .oldPrice(param.getOldPrice())
                        .quantity(param.getQuantity())
                        .imageUrl(param.getImageUrl())
                        .build()
        ).collect(Collectors.toList());

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .status("PENDING")
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

        return order;
    }

    @lombok.Data
    @lombok.Builder
    public static class OrderItemParam {
        private String productId;
        private String variantId;
        private String categoryName;
        private String productName;
        private String variantDescription;
        private Double price;
        private Double oldPrice;
        private Integer quantity;
        private String imageUrl;
    }
}
