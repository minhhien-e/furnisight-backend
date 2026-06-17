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
import com.furnisight.order.domain.services.dto.OrderDraft;

@Service
@RequiredArgsConstructor
public class OrderLifecycle {

    private final OrderCodeGenerator orderCodeGenerator;
    private final PricingService pricingService;

    public Order createPendingOrder(OrderDraft draft) {
        if (draft.getItems() == null || draft.getItems().isEmpty()) {
            throw new ValidationException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        var pricing = draft.getPricingSummary();
        return createPendingOrder(
                draft.getUserId(),
                draft.getCustomerEmail(),
                draft.getCustomerNote(),
                draft.getAddressInfo().toShippingDetail(),
                draft.paymentDetailOrDefault(),
                draft.getItems(),
                draft.getShopVoucherCode(),
                draft.getShippingVoucherCode(),
                draft.getComboId(),
                pricing != null ? pricing.getVoucherDiscount() : 0.0,
                pricing != null ? pricing.getShippingDiscount() : 0.0,
                pricing != null ? pricing.getComboDiscount() : 0.0,
                pricing != null ? pricing.getShippingFee() : 0.0,
                pricing != null ? pricing.getInsuranceFee() : 0.0
        );
    }

    public Order createPendingOrder(
            UUID userId,
            String customerEmail,
            String customerNote,
            ShippingDetail shippingDetail,
            PaymentDetail paymentDetail,
            List<OrderItemParam> itemParams,
            String shopVoucherCode,
            String shippingVoucherCode,
            String comboId,
            Double discountAmount,
            Double shippingDiscount,
            Double comboDiscount,
            Double shippingFee,
            Double insuranceFee) {

        if (itemParams == null || itemParams.isEmpty()) {
            throw new ValidationException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        // Use provided fees or default to 0
        double actualShippingFee = shippingFee != null ? shippingFee : 0.0;
        double actualInsuranceFee = insuranceFee != null ? insuranceFee : 0.0;
        double actualShippingDiscount = shippingDiscount != null ? shippingDiscount : 0.0;
        double actualDiscountAmount = discountAmount != null ? discountAmount : 0.0;
        double actualComboDiscount = comboDiscount != null ? comboDiscount : 0.0;

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
                        .quantity(param.getQuantity())
                        .build()
        ).collect(Collectors.toList());

        String orderCode = orderCodeGenerator.generate();

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode(orderCode)
                .status(OrderStatus.UNPAID)
                .fee(OrderFee.builder()
                        .shippingFee(actualShippingFee)
                        .shippingDiscount(actualShippingDiscount)
                        .discountAmount(actualDiscountAmount)
                        .comboDiscount(actualComboDiscount)
                        .insuranceFee(actualInsuranceFee)
                        .shopVoucherCode(shopVoucherCode)
                        .shippingVoucherCode(shippingVoucherCode)
                        .comboId(comboId)
                        .build())
                .customerEmail(customerEmail)
                .customerNote(customerNote)
                .shippingDetail(shippingDetail)
                .paymentDetail(paymentDetail)
                .items(items)
                .build();
                
        pricingService.calculateOrderTotals(order);
        
        return order;
    }
}
