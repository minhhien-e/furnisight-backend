package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoucherStatsResponse {
    private long totalVouchers;
    private long activeVouchers;
    private long issuedCount;
    private long campaignCount;
    private long runningCampaignCount;
    private long activeCombos;
    private long comboUsedCount;
}
