package com.furnisight.admin.marketing.application;

import com.furnisight.admin.marketing.web.dto.MarketingCampaignRequest;
import com.furnisight.admin.marketing.web.dto.MarketingCampaignResponse;
import com.furnisight.admin.marketing.web.dto.MarketingComboRequest;
import com.furnisight.admin.marketing.web.dto.MarketingComboResponse;
import com.furnisight.admin.marketing.web.dto.MarketingNotificationRequest;
import com.furnisight.admin.marketing.web.dto.MarketingNotificationResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import com.furnisight.admin.voucher.infrastructure.PromotionAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketingService {
    private final PromotionAdminClient promotionAdminClient;

    public PageResponse<MarketingCampaignResponse> getCampaigns(String query, String status) { return promotionAdminClient.getMarketingCampaigns(query, status); }
    public ActionResultResponse createCampaign(MarketingCampaignRequest request) { return promotionAdminClient.createMarketingCampaign(request); }
    public ActionResultResponse updateCampaign(String id, MarketingCampaignRequest request) { return promotionAdminClient.updateMarketingCampaign(id, request); }
    public ActionResultResponse deleteCampaign(String id) { return promotionAdminClient.deleteMarketingCampaign(id); }

    public PageResponse<MarketingComboResponse> getCombos(String query, String status) { return promotionAdminClient.getMarketingCombos(query, status); }
    public ActionResultResponse createCombo(MarketingComboRequest request) { return promotionAdminClient.createMarketingCombo(request); }
    public ActionResultResponse updateCombo(String id, MarketingComboRequest request) { return promotionAdminClient.updateMarketingCombo(id, request); }
    public ActionResultResponse deleteCombo(String id) { return promotionAdminClient.deleteMarketingCombo(id); }

    public PageResponse<MarketingNotificationResponse> getNotifications(String query, String status) { return promotionAdminClient.getMarketingNotifications(query, status); }
    public ActionResultResponse createNotification(MarketingNotificationRequest request) { return promotionAdminClient.createMarketingNotification(request); }
    public ActionResultResponse updateNotification(String id, MarketingNotificationRequest request) { return promotionAdminClient.updateMarketingNotification(id, request); }
    public ActionResultResponse deleteNotification(String id) { return promotionAdminClient.deleteMarketingNotification(id); }
}
