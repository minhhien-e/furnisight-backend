package com.furnisight.admin.marketing.application;

import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.infrastructure.PromotionAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketingService {
    private final PromotionAdminClient promotionAdminClient;

    public Object getCampaigns(String query, String status) { return promotionAdminClient.getMarketingCampaigns(query, status); }
    public ActionResultResponse createCampaign(Object request) { return promotionAdminClient.createMarketingCampaign(request); }
    public ActionResultResponse updateCampaign(String id, Object request) { return promotionAdminClient.updateMarketingCampaign(id, request); }
    public ActionResultResponse deleteCampaign(String id) { return promotionAdminClient.deleteMarketingCampaign(id); }

    public Object getCombos(String query, String status) { return promotionAdminClient.getMarketingCombos(query, status); }
    public ActionResultResponse createCombo(Object request) { return promotionAdminClient.createMarketingCombo(request); }
    public ActionResultResponse updateCombo(String id, Object request) { return promotionAdminClient.updateMarketingCombo(id, request); }
    public ActionResultResponse deleteCombo(String id) { return promotionAdminClient.deleteMarketingCombo(id); }

    public Object getNotifications(String query, String status) { return promotionAdminClient.getMarketingNotifications(query, status); }
    public ActionResultResponse createNotification(Object request) { return promotionAdminClient.createMarketingNotification(request); }
    public ActionResultResponse updateNotification(String id, Object request) { return promotionAdminClient.updateMarketingNotification(id, request); }
    public ActionResultResponse deleteNotification(String id) { return promotionAdminClient.deleteMarketingNotification(id); }
}
