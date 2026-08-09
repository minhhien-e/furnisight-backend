package com.furnisight.order.domain.services.dto;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import com.furnisight.order.domain.valueobjects.ShippingDetail;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderAddressInfo {
    String receiverName;
    String receiverPhone;
    String address;
    String shippingMethod;

    public ShippingDetail toShippingDetail() {
        validate();
        return ShippingDetail.builder()
                .shippingAddressName(receiverName)
                .shippingAddressPhone(receiverPhone)
                .shippingAddressDetail(address)
                .shippingMethod(shippingMethod)
                .build();
    }

    private void validate() {
        if (isBlank(receiverName) || isBlank(receiverPhone) || isBlank(address) || isBlank(shippingMethod)) {
            throw new ValidationException(ErrorCode.INVALID_SHIPPING_INFO);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
