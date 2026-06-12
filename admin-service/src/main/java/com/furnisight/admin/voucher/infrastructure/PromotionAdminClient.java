package com.furnisight.admin.voucher.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherListResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherStatsResponse;
import com.google.protobuf.Empty;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromotionAdminClient {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    @GrpcClient("promotion-service")
    private AdminPromotionServiceGrpc.AdminPromotionServiceBlockingStub promotionStub;

    public PromotionAdminClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public VoucherListResponse getVouchers(String query, String type, String status) {
        var response = promotionStub.getAdminVouchers(GetAdminVouchersRequest.newBuilder()
                .setQuery(value(query))
                .setType(value(type))
                .setStatus(value(status))
                .build());
        return new VoucherListResponse(response.getItemsList().stream()
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
                        voucher.getActive(),
                        voucher.getStatusLabel(),
                        voucher.getIssuedCount()))
                .toList());
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
        return action(promotionStub.deleteVoucher(DeleteVoucherRequest.newBuilder()
                .setId(value(id))
                .build()));
    }

    public ActionResultResponse publishVoucher(String id, Object request) {
        return action(promotionStub.publishVoucher(PublishVoucherRequest.newBuilder()
                .setId(value(id))
                .setPayloadJson(toJson(request))
                .build()));
    }

    public Object getMarketingCampaigns(String query, String status) {
        return marketingList(promotionStub.getMarketingCampaigns(listRequest(query, status)).getPayloadJson());
    }

    public ActionResultResponse createMarketingCampaign(Object request) {
        return action(promotionStub.createMarketingCampaign(jsonRequest(request)));
    }

    public ActionResultResponse updateMarketingCampaign(String id, Object request) {
        return action(promotionStub.updateMarketingCampaign(updateJsonRequest(id, request)));
    }

    public ActionResultResponse deleteMarketingCampaign(String id) {
        return action(promotionStub.deleteMarketingCampaign(deleteRequest(id)));
    }

    public Object getMarketingCombos(String query, String status) {
        return marketingList(promotionStub.getMarketingCombos(listRequest(query, status)).getPayloadJson());
    }

    public ActionResultResponse createMarketingCombo(Object request) {
        return action(promotionStub.createMarketingCombo(jsonRequest(request)));
    }

    public ActionResultResponse updateMarketingCombo(String id, Object request) {
        return action(promotionStub.updateMarketingCombo(updateJsonRequest(id, request)));
    }

    public ActionResultResponse deleteMarketingCombo(String id) {
        return action(promotionStub.deleteMarketingCombo(deleteRequest(id)));
    }

    public Object getMarketingNotifications(String query, String status) {
        return marketingList(promotionStub.getMarketingNotifications(listRequest(query, status)).getPayloadJson());
    }

    public ActionResultResponse createMarketingNotification(Object request) {
        return action(promotionStub.createMarketingNotification(jsonRequest(request)));
    }

    public ActionResultResponse updateMarketingNotification(String id, Object request) {
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

    private MarketingJsonRequest jsonRequest(Object request) {
        return MarketingJsonRequest.newBuilder()
                .setPayloadJson(toJson(request))
                .build();
    }

    private UpdateMarketingJsonRequest updateJsonRequest(String id, Object request) {
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

    private Map<String, Object> marketingList(String payloadJson) {
        try {
            return objectMapper.readValue(value(payloadJson).isBlank() ? "{}" : payloadJson, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse promotion gRPC response", ex);
        }
    }

    private String toJson(Object request) {
        try {
            return objectMapper.writeValueAsString(request == null ? Map.of() : request);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize promotion request", ex);
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
