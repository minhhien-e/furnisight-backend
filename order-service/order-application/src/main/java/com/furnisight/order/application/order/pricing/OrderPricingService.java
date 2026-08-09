package com.furnisight.order.application.order.pricing;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.validator.OrderItemValidationResult;
import com.furnisight.order.application.promotion.port.out.PromotionValidationPort;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersRequest;
import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.services.dto.OrderPricingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPricingService {
    private final PromotionValidationPort promotionValidationPort;

    public OrderPricingSummary calculate(CreateOrderCommand command, OrderItemValidationResult itemResult) {
        double voucherDiscount = 0.0;
        double shippingDiscount = 0.0;
        double comboDiscount = 0.0;
        double shippingFee = nonNegative(command.getShippingFee());
        double insuranceFee = nonNegative(command.getInsuranceFee());

        if (hasText(command.getShopVoucherCode()) || hasText(command.getShippingVoucherCode())) {
            var voucherResult = promotionValidationPort.validateOrderVouchers(ValidateOrderVouchersRequest.builder()
                    .userId(command.getUserId())
                    .shopVoucherCode(command.getShopVoucherCode())
                    .shippingVoucherCode(command.getShippingVoucherCode())
                    .subtotal(itemResult.getSubtotal())
                    .shippingFee(shippingFee)
                    .build());
            if (!voucherResult.isValid()) {
                throw new IllegalArgumentException(voucherResult.getMessage());
            }
            voucherDiscount = voucherResult.getDiscountAmount() != null ? voucherResult.getDiscountAmount() : 0.0;
            shippingDiscount = voucherResult.getShippingDiscount() != null ? voucherResult.getShippingDiscount() : 0.0;
        }

        if (hasText(command.getComboId())) {
            var comboResult = promotionValidationPort.validateCombo(ValidateComboRequest.builder()
                    .userId(command.getUserId())
                    .comboId(command.getComboId())
                    .items(itemResult.getComboItems())
                    .build());
            if (!comboResult.isValid()) {
                throw new IllegalArgumentException(comboResult.getMessage());
            }
            comboDiscount = comboResult.getComboDiscount() != null ? comboResult.getComboDiscount() : 0.0;
        }

        double totalAmount = Math.max(
                0.0,
                itemResult.getSubtotal() + shippingFee + insuranceFee
                        - voucherDiscount - shippingDiscount - comboDiscount
        );

        return OrderPricingSummary.builder()
                .subtotal(itemResult.getSubtotal())
                .voucherDiscount(voucherDiscount)
                .shippingDiscount(shippingDiscount)
                .comboDiscount(comboDiscount)
                .shippingFee(shippingFee)
                .insuranceFee(insuranceFee)
                .totalAmount(totalAmount)
                .build();
    }

    private double nonNegative(Double value) {
        if (value == null) {
            return 0.0;
        }
        if (value < 0.0) {
            throw new ValidationException(ErrorCode.INVALID_FEE);
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
