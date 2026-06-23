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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PromotionAdminClient {
    private final ObjectMapper objectMapper;

    @GrpcClient("promotion-service")
    private AdminPromotionServiceGrpc.AdminPromotionServiceBlockingStub promotionStub;

    public PromotionAdminClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<VoucherResponse> getVouchers(String query, String type, String status) {
        GetAdminVouchersRequest request = GetAdminVouchersRequest.newBuilder()
                .setQuery(value(query))
                .setType(value(type))
                .setStatus(value(status))
                .build();
        
        var response = promotionStub.getAdminVouchers(request);
        return response.getItemsList().stream()
                .map(voucher -> new VoucherResponse(
                        voucher.getId(),
                        voucher.getCode(),
                        voucher.getName(),
                        voucher.getDescription(),
                        voucher.getIcon(),
                        voucher.getVoucherType(),
                        voucher.getDiscountType(),
                        voucher.getDiscountValue(),
                        voucher.hasMaxDiscount() ? voucher.getMaxDiscount() : null,
                        voucher.hasMinOrder() ? voucher.getMinOrder() : null,
                        voucher.getStartDate(),
                        voucher.getEndDate(),
                        "", // createdAt not strictly needed
                        voucher.getActive(),
                        voucher.getStatusLabel(),
                        voucher.getIssuedCount()))
                .collect(Collectors.toList());
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
        return action(promotionStub.createVoucher(toGrpc(request)));
    }

    public ActionResultResponse updateVoucher(String id, UpsertVoucherRequest request) {
        return action(promotionStub.updateVoucher(UpdateVoucherRequest.newBuilder()
                .setId(value(id))
                .setVoucher(toGrpc(request))
                .build()));
    }

    public ActionResultResponse deleteVoucher(String id) {
        return action(promotionStub.deleteVoucher(DeleteVoucherRequest.newBuilder().setId(value(id)).build()));
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
}
