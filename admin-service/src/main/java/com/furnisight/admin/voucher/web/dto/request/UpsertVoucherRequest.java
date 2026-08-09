package com.furnisight.admin.voucher.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UpsertVoucherRequest(
        @NotBlank(message = "Code is required") String code,
        @NotBlank(message = "Name is required") String name,
        String description,
        String icon,
        @NotBlank(message = "Voucher type is required") String voucherType,
        @NotBlank(message = "Discount type is required") String discountType,
        @NotNull(message = "Discount value is required") @Min(0) Double discountValue,
        @Min(0) Double maxDiscount,
        @Min(0) Double minOrder,
        @NotBlank(message = "Start date is required") String startDate,
        @NotBlank(message = "End date is required") String endDate,
        Boolean active
) {
}
