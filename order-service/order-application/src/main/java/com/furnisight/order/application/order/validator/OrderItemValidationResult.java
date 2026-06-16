package com.furnisight.order.application.order.validator;

import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.domain.services.dto.OrderItemParam;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OrderItemValidationResult {
    List<OrderItemParam> items;
    List<ValidateComboRequest.Item> comboItems;
    double subtotal;
}
