package com.furnisight.order.application.promotion.port.out;

import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersResult;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboResult;

public interface PromotionValidationPort {
    ValidateOrderVouchersResult validateOrderVouchers(ValidateOrderVouchersRequest request);
    ValidateComboResult validateCombo(ValidateComboRequest request);
}
