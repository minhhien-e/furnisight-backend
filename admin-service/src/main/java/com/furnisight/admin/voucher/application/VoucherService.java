package com.furnisight.admin.voucher.application;

import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.infrastructure.PromotionAdminClient;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherListResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final PromotionAdminClient promotionAdminClient;

    public VoucherListResponse getVouchers(String query, String type, String status) {
        return promotionAdminClient.getVouchers(query, type, status);
    }

    public VoucherStatsResponse getStats() {
        return promotionAdminClient.getStats();
    }

    public ActionResultResponse createVoucher(UpsertVoucherRequest request) {
        return promotionAdminClient.createVoucher(request);
    }

    public ActionResultResponse updateVoucher(String id, UpsertVoucherRequest request) {
        return promotionAdminClient.updateVoucher(id, request);
    }

    public ActionResultResponse deleteVoucher(String id) {
        return promotionAdminClient.deleteVoucher(id);
    }

    public ActionResultResponse publishVoucher(String id, Object request) {
        return promotionAdminClient.publishVoucher(id, request);
    }
}
