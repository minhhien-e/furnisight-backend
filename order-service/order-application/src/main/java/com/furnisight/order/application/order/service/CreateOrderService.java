package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.application.order.port.in.dto.OrderCreateProjection;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherCommand;
import com.furnisight.order.application.promotion.port.in.dto.ValidateVoucherResponse;
import com.furnisight.order.application.promotion.port.in.usecase.VoucherUseCase;
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
    private final VoucherUseCase voucherUseCase;
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
                .paymentStatus("UNPAID")
                .paidAmount(0.0)
                .build();

        // Calculate subtotal for voucher validation using PricingService
        double subTotal = pricingService.calculateSubTotal(itemParams);

        // Validate Shop Voucher
        double actualDiscountAmount = 0.0;
        if (command.getShopVoucherCode() != null && !command.getShopVoucherCode().isEmpty()) {
            ValidateVoucherResponse shopResp = voucherUseCase.validateVoucher(ValidateVoucherCommand.builder()
                    .userId(command.getUserId())
                    .code(command.getShopVoucherCode())
                    .type("shop")
                    .subtotal(subTotal)
                    .build());
            if (shopResp.isValid()) {
                actualDiscountAmount = shopResp.getDiscount();
            } else {
                throw new IllegalArgumentException(shopResp.getMessage());
            }
        }

        // Validate Shipping Voucher
        double actualShippingDiscount = 0.0;
        if (command.getShippingVoucherCode() != null && !command.getShippingVoucherCode().isEmpty()) {
            ValidateVoucherResponse shipResp = voucherUseCase.validateVoucher(ValidateVoucherCommand.builder()
                    .userId(command.getUserId())
                    .code(command.getShippingVoucherCode())
                    .type("ship")
                    .subtotal(subTotal)
                    .build());
            if (shipResp.isValid()) {
                actualShippingDiscount = shipResp.getDiscount();
            } else {
                throw new IllegalArgumentException(shipResp.getMessage());
            }
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
                actualDiscountAmount,
                actualShippingDiscount,
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
}
