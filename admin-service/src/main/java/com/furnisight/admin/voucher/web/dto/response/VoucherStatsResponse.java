package com.furnisight.admin.voucher.web.dto.response;

public record VoucherStatsResponse(
        long totalVouchers,
        long activeVouchers,
        long issuedCount,
        long campaignCount,
        long runningCampaignCount,
        long activeCombos,
        long comboUsedCount
) {
}
