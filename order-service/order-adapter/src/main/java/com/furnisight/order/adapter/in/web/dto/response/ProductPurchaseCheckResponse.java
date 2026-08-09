package com.furnisight.order.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductPurchaseCheckResponse {
    private boolean purchased;
    private String orderItemId;
}
