package com.furnisight.order.application.order.pricing;

import com.furnisight.order.application.order.port.in.command.CreateOrderCommand;
import com.furnisight.order.application.order.validator.OrderItemValidationResult;
import com.furnisight.order.application.promotion.port.out.PromotionValidationPort;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersResult;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPricingServiceTest {
    @Test
    void ignoresClientDiscountAmountsAndUsesPromotionValidationResult() {
        ValidateOrderVouchersResult validated = new ValidateOrderVouchersResult();
        validated.setValid(true);
        validated.setDiscountAmount(120.0);
        validated.setShippingDiscount(30.0);
        PromotionValidationPort validationPort = new PromotionValidationPort() {
            @Override
            public ValidateOrderVouchersResult validateOrderVouchers(ValidateOrderVouchersRequest request) {
                return validated;
            }

            @Override
            public ValidateComboResult validateCombo(ValidateComboRequest request) {
                throw new AssertionError("Combo validation should not be called");
            }
        };

        CreateOrderCommand command = CreateOrderCommand.builder()
                .userId(UUID.randomUUID())
                .shopVoucherCode("SHOP")
                .shippingVoucherCode("SHIP")
                .discountAmount(9999.0)
                .shippingDiscount(9999.0)
                .shippingFee(50.0)
                .insuranceFee(20.0)
                .build();
        var items = OrderItemValidationResult.builder()
                .subtotal(1000.0)
                .items(List.of())
                .comboItems(List.of())
                .build();

        var result = new OrderPricingService(validationPort).calculate(command, items);

        assertThat(result.getVoucherDiscount()).isEqualTo(120.0);
        assertThat(result.getShippingDiscount()).isEqualTo(30.0);
        assertThat(result.getTotalAmount()).isEqualTo(920.0);
    }
}
