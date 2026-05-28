package com.furnisight.order.domain.valueobjects;

import com.furnisight.order.domain.exceptions.ErrorCode;
import com.furnisight.order.domain.exceptions.ValidationException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingDetail {
    private String shippingAddressName;
    private String shippingAddressPhone;
    private String shippingAddressDetail;
    private String shippingMethod;

    @Builder
    public ShippingDetail(String shippingAddressName, String shippingAddressPhone, 
                          String shippingAddressDetail, String shippingMethod) {
        if (shippingAddressName == null || shippingAddressName.trim().isEmpty() ||
            shippingAddressPhone == null || shippingAddressPhone.trim().isEmpty() ||
            shippingAddressDetail == null || shippingAddressDetail.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_SHIPPING_INFO);
        }
        
        this.shippingAddressName = shippingAddressName;
        this.shippingAddressPhone = shippingAddressPhone;
        this.shippingAddressDetail = shippingAddressDetail;
        this.shippingMethod = shippingMethod;
    }
}
