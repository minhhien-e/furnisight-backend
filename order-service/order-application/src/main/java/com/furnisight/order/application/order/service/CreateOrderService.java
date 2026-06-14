package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.application.order.port.in.dto.OrderCreateProjection;
import com.furnisight.order.application.promotion.port.out.PromotionValidationPort;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersRequest;
import com.furnisight.order.domain.repository.order.OrderRepository;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.services.OrderLifecycle;
import com.furnisight.order.domain.services.PricingService;
import com.furnisight.order.domain.services.dto.OrderItemParam;
import com.furnisight.order.domain.valueobjects.ShippingDetail;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderLifecycle orderLifecycle;
    private final PromotionValidationPort promotionValidationPort;
    private final PricingService pricingService;

    @Override
    @Transactional
    public OrderCreateProjection createOrder(CreateOrderCommand command) {
        // Map Application Command to Domain Parameter
        var itemParams = command.getItems() == null ? null : command.getItems().stream()
                .map(item -> OrderItemParam.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .categoryName(item.getCategoryName())
                        .productName(item.getProductName())
                        .price(item.getPrice())
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
                .paymentStatus("UNPAID")
                .paidAmount(0.0)
                .build();

        // Calculate subtotal for voucher validation using PricingService
        double subTotal = pricingService.calculateSubTotal(itemParams);

        double actualDiscountAmount = 0.0;
        double actualShippingDiscount = 0.0;
        double actualComboDiscount = 0.0;
        if (hasText(command.getShopVoucherCode()) || hasText(command.getShippingVoucherCode())) {
            var voucherResult = promotionValidationPort.validateOrderVouchers(ValidateOrderVouchersRequest.builder()
                    .userId(command.getUserId())
                    .shopVoucherCode(command.getShopVoucherCode())
                    .shippingVoucherCode(command.getShippingVoucherCode())
                    .subtotal(subTotal)
                    .shippingFee(command.getShippingFee())
                    .build());
            if (!voucherResult.isValid()) {
                throw new IllegalArgumentException(voucherResult.getMessage());
            }
            actualDiscountAmount = voucherResult.getDiscountAmount() != null ? voucherResult.getDiscountAmount() : 0.0;
            actualShippingDiscount = voucherResult.getShippingDiscount() != null ? voucherResult.getShippingDiscount() : 0.0;
        }
        if (hasText(command.getComboId())) {
            var comboResult = promotionValidationPort.validateCombo(ValidateComboRequest.builder()
                    .userId(command.getUserId())
                    .comboId(command.getComboId())
                    .items(command.getItems() == null ? java.util.List.of() : command.getItems().stream()
                            .map(item -> ValidateComboRequest.Item.builder()
                                    .productId(item.getProductId())
                                    .variantId(item.getVariantId())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .build())
                            .toList())
                    .build());
            if (!comboResult.isValid()) {
                throw new IllegalArgumentException(comboResult.getMessage());
            }
            actualComboDiscount = comboResult.getComboDiscount() != null ? comboResult.getComboDiscount() : 0.0;
        }

        // Delegate core business logic to Domain Service
        Order order = orderLifecycle.createPendingOrder(
                command.getUserId(),
                command.getCustomerNote(),
                shippingDetail,
                paymentDetail,
                itemParams,
                command.getShopVoucherCode(),
                command.getShippingVoucherCode(),
                command.getComboId(),
                actualDiscountAmount,
                actualShippingDiscount,
                actualComboDiscount,
                command.getShippingFee(),
                command.getInsuranceFee()
        );

        Order savedOrder = orderRepository.save(order);

        return OrderCreateProjection.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .status(savedOrder.getStatus().name())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
