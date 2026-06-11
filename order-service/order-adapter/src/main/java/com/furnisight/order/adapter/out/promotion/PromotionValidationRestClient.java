package com.furnisight.order.adapter.out.promotion;

import com.furnisight.order.application.promotion.port.out.PromotionValidationPort;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboResult;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PromotionValidationRestClient implements PromotionValidationPort {
    private final RestClient restClient;

    public PromotionValidationRestClient(
            RestClient.Builder builder,
            @Value("${PROMOTION_SERVICE_URL:http://localhost:8086}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl + "/api/v1").build();
    }

    @Override
    public ValidateOrderVouchersResult validateOrderVouchers(ValidateOrderVouchersRequest request) {
        return restClient.post()
                .uri("/internal/vouchers/validate-order")
                .body(request)
                .retrieve()
                .body(ValidateOrderVouchersResult.class);
    }

    @Override
    public ValidateComboResult validateCombo(ValidateComboRequest request) {
        return restClient.post()
                .uri("/internal/combos/validate-order")
                .body(request)
                .retrieve()
                .body(ValidateComboResult.class);
    }
}
