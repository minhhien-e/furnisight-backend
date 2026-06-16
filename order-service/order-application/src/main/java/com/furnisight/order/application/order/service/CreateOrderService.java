package com.furnisight.order.application.order.service;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.port.in.usecase.CreateOrderUseCase;
import com.furnisight.order.application.order.port.in.dto.OrderCreateProjection;
import com.furnisight.order.application.order.pricing.OrderPricingService;
import com.furnisight.order.application.order.validator.OrderItemValidationService;
import com.furnisight.order.domain.entities.order.Order;
import com.furnisight.order.domain.enums.PaymentType;
import com.furnisight.order.application.processing.OrderOperation;
import com.furnisight.order.application.processing.OrderProcessingCommand;
import com.furnisight.order.application.processing.OrderProcessingService;
import com.furnisight.order.domain.services.OrderLifecycle;
import com.furnisight.order.domain.services.dto.OrderAddressInfo;
import com.furnisight.order.domain.services.dto.OrderDraft;
import com.furnisight.order.domain.valueobjects.PaymentDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderLifecycle orderLifecycle;
    private final OrderItemValidationService itemValidationService;
    private final OrderPricingService orderPricingService;
    private final OrderProcessingService orderProcessingService;

    @Override
    @Transactional
    public OrderCreateProjection createOrder(CreateOrderCommand command) {
        var addressInfo = OrderAddressInfo.builder()
                .receiverName(command.getShippingAddressName())
                .receiverPhone(command.getShippingAddressPhone())
                .address(command.getShippingAddressDetail())
                .shippingMethod(command.getShippingMethod())
                .build();
        var itemResult = itemValidationService.validate(command);
        var pricingSummary = orderPricingService.calculate(command, itemResult);
        Order order = orderLifecycle.createPendingOrder(OrderDraft.builder()
                .userId(command.getUserId())
                .customerNote(command.getCustomerNote())
                .addressInfo(addressInfo)
                .items(itemResult.getItems())
                .pricingSummary(pricingSummary)
                .paymentDetail(initialPaymentDetail(command))
                .shopVoucherCode(command.getShopVoucherCode())
                .shippingVoucherCode(command.getShippingVoucherCode())
                .comboId(command.getComboId())
                .build());

        Order savedOrder = orderProcessingService.process(OrderProcessingCommand.builder()
                .operation(OrderOperation.CREATE)
                .order(order)
                .paymentType(PaymentType.from(command.getPaymentMethod()))
                .actorId(command.getUserId())
                .actorType("CUSTOMER")
                .build()).order();

        return OrderCreateProjection.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .status(savedOrder.getStatus().name())
                .build();
    }

    private PaymentDetail initialPaymentDetail(CreateOrderCommand command) {
        return PaymentDetail.builder()
                .paymentMethod(command.getPaymentMethod())
                .paymentStatus("UNPAID")
                .paidAmount(0.0)
                .build();
    }
}
