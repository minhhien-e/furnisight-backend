package com.furnisight.admin.voucher.infrastructure;

import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherListResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherStatsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PromotionAdminClient {
    private final RestClient restClient;

    public PromotionAdminClient(
            RestClient.Builder builder,
            @Value("${PROMOTION_SERVICE_URL:http://localhost:8086}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl + "/api/v1").build();
    }

    public VoucherListResponse getVouchers(String query, String type, String status) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/admin/vouchers")
                        .queryParam("query", query == null ? "" : query)
                        .queryParam("type", type == null ? "" : type)
                        .queryParam("status", status == null ? "" : status)
                        .build())
                .retrieve()
                .body(VoucherListResponse.class);
    }

    public VoucherStatsResponse getStats() {
        return restClient.get()
                .uri("/internal/admin/vouchers/stats")
                .retrieve()
                .body(VoucherStatsResponse.class);
    }

    public ActionResultResponse createVoucher(UpsertVoucherRequest request) {
        return restClient.post()
                .uri("/internal/admin/vouchers")
                .body(request)
                .retrieve()
                .body(ActionResultResponse.class);
    }

    public ActionResultResponse updateVoucher(String id, UpsertVoucherRequest request) {
        return restClient.put()
                .uri("/internal/admin/vouchers/{id}", id)
                .body(request)
                .retrieve()
                .body(ActionResultResponse.class);
    }

    public ActionResultResponse deleteVoucher(String id) {
        return restClient.delete()
                .uri("/internal/admin/vouchers/{id}", id)
                .retrieve()
                .body(ActionResultResponse.class);
    }

    public ActionResultResponse publishVoucher(String id, Object request) {
        return restClient.post()
                .uri("/internal/admin/vouchers/{id}/publish", id)
                .body(request)
                .retrieve()
                .body(ActionResultResponse.class);
    }

    public Object getMarketingCampaigns(String query, String status) {
        return getMarketingList("/internal/admin/marketing/campaigns", query, status);
    }

    public ActionResultResponse createMarketingCampaign(Object request) {
        return postMarketing("/internal/admin/marketing/campaigns", request);
    }

    public ActionResultResponse updateMarketingCampaign(String id, Object request) {
        return putMarketing("/internal/admin/marketing/campaigns/{id}", id, request);
    }

    public ActionResultResponse deleteMarketingCampaign(String id) {
        return deleteMarketing("/internal/admin/marketing/campaigns/{id}", id);
    }

    public Object getMarketingCombos(String query, String status) {
        return getMarketingList("/internal/admin/marketing/combos", query, status);
    }

    public ActionResultResponse createMarketingCombo(Object request) {
        return postMarketing("/internal/admin/marketing/combos", request);
    }

    public ActionResultResponse updateMarketingCombo(String id, Object request) {
        return putMarketing("/internal/admin/marketing/combos/{id}", id, request);
    }

    public ActionResultResponse deleteMarketingCombo(String id) {
        return deleteMarketing("/internal/admin/marketing/combos/{id}", id);
    }

    public Object getMarketingNotifications(String query, String status) {
        return getMarketingList("/internal/admin/marketing/notifications", query, status);
    }

    public ActionResultResponse createMarketingNotification(Object request) {
        return postMarketing("/internal/admin/marketing/notifications", request);
    }

    public ActionResultResponse updateMarketingNotification(String id, Object request) {
        return putMarketing("/internal/admin/marketing/notifications/{id}", id, request);
    }

    public ActionResultResponse deleteMarketingNotification(String id) {
        return deleteMarketing("/internal/admin/marketing/notifications/{id}", id);
    }

    private Object getMarketingList(String path, String query, String status) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("query", query == null ? "" : query)
                        .queryParam("status", status == null ? "" : status)
                        .build())
                .retrieve()
                .body(Map.class);
    }

    private ActionResultResponse postMarketing(String path, Object request) {
        return restClient.post().uri(path).body(request).retrieve().body(ActionResultResponse.class);
    }

    private ActionResultResponse putMarketing(String path, String id, Object request) {
        return restClient.put().uri(path, id).body(request).retrieve().body(ActionResultResponse.class);
    }

    private ActionResultResponse deleteMarketing(String path, String id) {
        return restClient.delete().uri(path, id).retrieve().body(ActionResultResponse.class);
    }
}
