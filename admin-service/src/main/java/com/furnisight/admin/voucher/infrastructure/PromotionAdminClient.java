package com.furnisight.admin.voucher.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.admin.promotion.AdminActionResponse;
import com.furnisight.admin.promotion.AdminPromotionServiceGrpc;
import com.furnisight.admin.promotion.DeleteMarketingRequest;
import com.furnisight.admin.promotion.DeleteVoucherRequest;
import com.furnisight.admin.promotion.GetAdminVouchersRequest;
import com.furnisight.admin.promotion.GetMarketingListRequest;
import com.furnisight.admin.promotion.MarketingJsonRequest;
import com.furnisight.admin.promotion.PublishVoucherRequest;
import com.furnisight.admin.promotion.SaveVoucherRequest;
import com.furnisight.admin.promotion.UpdateMarketingJsonRequest;
import com.furnisight.admin.promotion.UpdateVoucherRequest;
import com.furnisight.admin.marketing.web.dto.MarketingCampaignRequest;
import com.furnisight.admin.marketing.web.dto.MarketingCampaignResponse;
import com.furnisight.admin.marketing.web.dto.MarketingComboRequest;
import com.furnisight.admin.marketing.web.dto.MarketingComboResponse;
import com.furnisight.admin.marketing.web.dto.MarketingNotificationRequest;
import com.furnisight.admin.marketing.web.dto.MarketingNotificationResponse;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.shared.web.PageResponse;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherStatsResponse;
import com.google.protobuf.Empty;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Component
public class PromotionAdminClient {
    private final ObjectMapper objectMapper;
    private final RestClient promotionRestClient;

    @GrpcClient("promotion-service")
    private AdminPromotionServiceGrpc.AdminPromotionServiceBlockingStub promotionStub;

    public PromotionAdminClient(
            ObjectMapper objectMapper,
            @Value("${promotion.service.http-url:http://promotion-service:8080}") String promotionServiceHttpUrl) {
        this.objectMapper = objectMapper;
        this.promotionRestClient = RestClient.builder()
                .baseUrl(stripTrailingSlash(promotionServiceHttpUrl) + "/api/v1")
                .build();
    }

    public List<VoucherResponse> getVouchers(String query, String type, String status) {
        PromotionListPayload response = promotionRestClient.get()
                .uri(uri -> uri.path("/internal/admin/vouchers")
                        .queryParam("query", value(query))
                        .queryParam("type", value(type))
                        .queryParam("status", value(status))
                        .build())
                .retrieve()
                .body(PromotionListPayload.class);
        List<PromotionPayload> items = Objects.requireNonNull(response, "Promotion voucher response is missing").items();
        Objects.requireNonNull(items, "Promotion voucher items are missing");
        return items.stream()
                .map(voucher -> new VoucherResponse(
                        voucher.id(),
                        voucher.code(),
                        voucher.name(),
                        voucher.description(),
                        voucher.icon(),
                        voucher.voucherType(),
                        voucher.discountType(),
                        voucher.discountValue(),
                        voucher.maxDiscount(),
                        voucher.minOrder(),
                        voucher.startDate(),
                        voucher.endDate(),
                        voucher.createdAt(),
                        voucher.active(),
                        voucher.placements(),
                        voucher.statusLabel(),
                        voucher.issuedCount()))
                .toList();
    }

    public VoucherStatsResponse getStats() {
        var stats = promotionStub.getVoucherStats(Empty.getDefaultInstance());
        return new VoucherStatsResponse(
                stats.getTotalVouchers(),
                stats.getActiveVouchers(),
                stats.getIssuedCount(),
                stats.getCampaignCount(),
                stats.getRunningCampaignCount(),
                stats.getActiveCombos(),
                stats.getComboUsedCount());
    }

    public ActionResultResponse createVoucher(UpsertVoucherRequest request) {
        ActionResultResponse response = promotionRestClient.post()
                .uri("/internal/admin/vouchers")
                .body(request)
                .retrieve()
                .body(ActionResultResponse.class);
        return response == null ? new ActionResultResponse(true, "Voucher created") : response;
    }

    public ActionResultResponse updateVoucher(String id, UpsertVoucherRequest request) {
        ActionResultResponse response = promotionRestClient.put()
                .uri("/internal/admin/vouchers/{id}", id)
                .body(request)
                .retrieve()
                .body(ActionResultResponse.class);
        return response == null ? new ActionResultResponse(true, "Voucher updated") : response;
    }

    public ActionResultResponse deleteVoucher(String id) {
        ActionResultResponse response = promotionRestClient.delete()
                .uri("/internal/admin/vouchers/{id}", id)
                .retrieve()
                .body(ActionResultResponse.class);
        return response == null ? new ActionResultResponse(true, "Voucher deleted") : response;
    }

    public ActionResultResponse publishVoucher(String id, com.furnisight.admin.voucher.web.dto.request.PublishVoucherRequest request) {
        return action(promotionStub.publishVoucher(PublishVoucherRequest.newBuilder()
                .setId(value(id))
                .setPayloadJson(toJson(request))
                .build()));
    }

    public PageResponse<MarketingCampaignResponse> getMarketingCampaigns(String query, String status) {
        return marketingList(promotionStub.getMarketingCampaigns(listRequest(query, status)).getPayloadJson(), MarketingCampaignResponse.class);
    }

    public ActionResultResponse createMarketingCampaign(MarketingCampaignRequest request) {
        return action(promotionStub.createMarketingCampaign(jsonRequest(request)));
    }

    public ActionResultResponse updateMarketingCampaign(String id, MarketingCampaignRequest request) {
        return action(promotionStub.updateMarketingCampaign(updateJsonRequest(id, request)));
    }

    public ActionResultResponse deleteMarketingCampaign(String id) {
        return action(promotionStub.deleteMarketingCampaign(deleteRequest(id)));
    }

    public PageResponse<MarketingComboResponse> getMarketingCombos(String query, String status) {
        return marketingList(promotionStub.getMarketingCombos(listRequest(query, status)).getPayloadJson(), MarketingComboResponse.class);
    }

    public ActionResultResponse createMarketingCombo(MarketingComboRequest request) {
        return action(promotionStub.createMarketingCombo(jsonRequest(request)));
    }

    public ActionResultResponse updateMarketingCombo(String id, MarketingComboRequest request) {
        return action(promotionStub.updateMarketingCombo(updateJsonRequest(id, request)));
    }

    public ActionResultResponse deleteMarketingCombo(String id) {
        return action(promotionStub.deleteMarketingCombo(deleteRequest(id)));
    }

    public PageResponse<MarketingNotificationResponse> getMarketingNotifications(String query, String status) {
        return marketingList(promotionStub.getMarketingNotifications(listRequest(query, status)).getPayloadJson(), MarketingNotificationResponse.class);
    }

    public ActionResultResponse createMarketingNotification(MarketingNotificationRequest request) {
        return action(promotionStub.createMarketingNotification(jsonRequest(request)));
    }

    public ActionResultResponse updateMarketingNotification(String id, MarketingNotificationRequest request) {
        return action(promotionStub.updateMarketingNotification(updateJsonRequest(id, request)));
    }

    public ActionResultResponse deleteMarketingNotification(String id) {
        return action(promotionStub.deleteMarketingNotification(deleteRequest(id)));
    }

    private SaveVoucherRequest toGrpc(UpsertVoucherRequest request) {
        SaveVoucherRequest.Builder builder = SaveVoucherRequest.newBuilder()
                .setCode(value(request.code()))
                .setName(value(request.name()))
                .setDescription(value(request.description()))
                .setIcon(value(request.icon()))
                .setVoucherType(value(request.voucherType()))
                .setDiscountType(value(request.discountType()))
                .setStartDate(value(request.startDate()))
                .setEndDate(value(request.endDate()));
        if (request.discountValue() != null) {
            builder.setDiscountValue(request.discountValue());
        }
        if (request.maxDiscount() != null) {
            builder.setMaxDiscount(request.maxDiscount());
        }
        if (request.minOrder() != null) {
            builder.setMinOrder(request.minOrder());
        }
        if (request.active() != null) {
            builder.setActive(request.active());
        }
        return builder.build();
    }

    private GetMarketingListRequest listRequest(String query, String status) {
        return GetMarketingListRequest.newBuilder()
                .setQuery(value(query))
                .setStatus(value(status))
                .build();
    }

    private MarketingJsonRequest jsonRequest(MarketingCampaignRequest request) {
        return MarketingJsonRequest.newBuilder()
                .setPayloadJson(toJson(request))
                .build();
    }

    private MarketingJsonRequest jsonRequest(MarketingComboRequest request) {
        return MarketingJsonRequest.newBuilder()
                .setPayloadJson(toJson(request))
                .build();
    }

    private MarketingJsonRequest jsonRequest(MarketingNotificationRequest request) {
        return MarketingJsonRequest.newBuilder()
                .setPayloadJson(toJson(request))
                .build();
    }

    private UpdateMarketingJsonRequest updateJsonRequest(String id, MarketingCampaignRequest request) {
        return UpdateMarketingJsonRequest.newBuilder()
                .setId(value(id))
                .setPayloadJson(toJson(request))
                .build();
    }

    private UpdateMarketingJsonRequest updateJsonRequest(String id, MarketingComboRequest request) {
        return UpdateMarketingJsonRequest.newBuilder()
                .setId(value(id))
                .setPayloadJson(toJson(request))
                .build();
    }

    private UpdateMarketingJsonRequest updateJsonRequest(String id, MarketingNotificationRequest request) {
        return UpdateMarketingJsonRequest.newBuilder()
                .setId(value(id))
                .setPayloadJson(toJson(request))
                .build();
    }

    private DeleteMarketingRequest deleteRequest(String id) {
        return DeleteMarketingRequest.newBuilder().setId(value(id)).build();
    }

    private ActionResultResponse action(AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private <T> PageResponse<T> marketingList(String payloadJson, Class<T> itemType) {
        try {
            var type = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, itemType);
            return objectMapper.readValue(value(payloadJson).isBlank() ? "{}" : payloadJson, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse promotion gRPC response", ex);
        }
    }

    private String toJson(MarketingCampaignRequest request) {
        return writeJson(request);
    }

    private String toJson(MarketingComboRequest request) {
        return writeJson(request);
    }

    private String toJson(MarketingNotificationRequest request) {
        return writeJson(request);
    }

    private String toJson(com.furnisight.admin.voucher.web.dto.request.PublishVoucherRequest request) {
        return writeJson(request);
    }

    private String writeJson(Object request) {
        try {
            return objectMapper.writeValueAsString(Objects.requireNonNull(request, "Promotion request is missing"));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize promotion request", ex);
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) return "http://promotion-service:8080";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record PromotionListPayload(List<PromotionPayload> items) {}

    private record PromotionPayload(
            String id,
            String code,
            String name,
            String description,
            String icon,
            String voucherType,
            String discountType,
            Double discountValue,
            Double maxDiscount,
            Double minOrder,
            String startDate,
            String endDate,
            String createdAt,
            boolean active,
            List<String> placements,
            String statusLabel,
            long issuedCount
    ) {}
}
