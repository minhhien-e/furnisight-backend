package com.furnisight.order.domain.valueobjects.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDetail {
    private String shippingAddressName;
    private String shippingAddressPhone;
    private String shippingAddressDetail;
    private String shippingMethod;
}
