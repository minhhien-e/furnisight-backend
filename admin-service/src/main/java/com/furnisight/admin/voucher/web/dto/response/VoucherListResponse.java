package com.furnisight.admin.voucher.web.dto.response;

import java.util.List;

public record VoucherListResponse(List<VoucherResponse> items) {
    public VoucherListResponse {
        items = items == null ? List.of() : items;
    }
}
