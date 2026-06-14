package com.furnisight.promotion.adapter.in.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.admin.promotion.AdminActionResponse;
import com.furnisight.admin.promotion.AdminPromotionServiceGrpc;
import com.furnisight.admin.promotion.DeleteMarketingRequest;
import com.furnisight.admin.promotion.DeleteVoucherRequest;
import com.furnisight.admin.promotion.GetAdminVouchersRequest;
import com.furnisight.admin.promotion.GetMarketingListRequest;
import com.furnisight.admin.promotion.MarketingJsonRequest;
import com.furnisight.admin.promotion.MarketingJsonResponse;
import com.furnisight.admin.promotion.PublishVoucherRequest;
import com.furnisight.admin.promotion.SaveVoucherRequest;
import com.furnisight.admin.promotion.UpdateMarketingJsonRequest;
import com.furnisight.admin.promotion.UpdateVoucherRequest;
import com.furnisight.admin.promotion.ValidateOrderComboRequest;
import com.furnisight.admin.promotion.ValidateOrderComboResponse;
import com.furnisight.admin.promotion.ValidateOrderVouchersRequest;
import com.furnisight.admin.promotion.ValidateOrderVouchersResponse;
import com.furnisight.admin.promotion.VoucherDto;
import com.furnisight.admin.promotion.VoucherListResponse;
import com.furnisight.promotion.application.dto.MarketingListResponse;
import com.furnisight.promotion.application.dto.PublishVoucherCommand;
import com.furnisight.promotion.application.dto.SaveMarketingCampaignCommand;
import com.furnisight.promotion.application.dto.SaveMarketingComboCommand;
import com.furnisight.promotion.application.dto.SaveMarketingNotificationCommand;
import com.furnisight.promotion.application.dto.SavePromotionCommand;
import com.furnisight.promotion.application.dto.ValidateComboCommand;
import com.furnisight.promotion.application.dto.ValidateOrderVouchersCommand;
import com.furnisight.promotion.application.dto.VoucherStatsResponse;
import com.furnisight.promotion.application.service.MarketingService;
import com.furnisight.promotion.application.service.PromotionService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class AdminPromotionGrpcService extends AdminPromotionServiceGrpc.AdminPromotionServiceImplBase {

    private final PromotionService promotionService;
    private final MarketingService marketingService;
    private final ObjectMapper objectMapper;

    @Override
    public void getAdminVouchers(GetAdminVouchersRequest request, StreamObserver<VoucherListResponse> responseObserver) {
        VoucherListResponse.Builder response = VoucherListResponse.newBuilder();
        promotionService.getAdminVouchers(blankToNull(request.getQuery()), blankToNull(request.getType()), blankToNull(request.getStatus()))
                .forEach(voucher -> {
                    VoucherDto.Builder builder = VoucherDto.newBuilder()
                            .setId(value(voucher.getId()))
                            .setCode(value(voucher.getCode()))
                            .setName(value(voucher.getName()))
                            .setDescription(value(voucher.getDescription()))
                            .setIcon(value(voucher.getIcon()))
                            .setVoucherType(value(voucher.getVoucherType()))
                            .setDiscountType(value(voucher.getDiscountType()))
                            .setDiscountValue(number(voucher.getDiscountValue()))
                            .setStartDate(format(voucher.getStartDate()))
                            .setEndDate(format(voucher.getEndDate()))
                            .setActive(voucher.isActive())
                            .setStatusLabel(value(voucher.getStatusLabel()))
                            .setIssuedCount(voucher.getIssuedCount());
                    if (voucher.getMaxDiscount() != null) {
                        builder.setMaxDiscount(voucher.getMaxDiscount());
                    }
                    if (voucher.getMinOrder() != null) {
                        builder.setMinOrder(voucher.getMinOrder());
                    }
                    response.addItems(builder.build());
                });
        complete(responseObserver, response.build());
    }

    @Override
    public void getVoucherStats(Empty request, StreamObserver<com.furnisight.admin.promotion.VoucherStatsResponse> responseObserver) {
        VoucherStatsResponse stats = promotionService.getStats();
        complete(responseObserver, com.furnisight.admin.promotion.VoucherStatsResponse.newBuilder()
                .setTotalVouchers(stats.getTotalVouchers())
                .setActiveVouchers(stats.getActiveVouchers())
                .setIssuedCount(stats.getIssuedCount())
                .setCampaignCount(stats.getCampaignCount())
                .setRunningCampaignCount(stats.getRunningCampaignCount())
                .setActiveCombos(stats.getActiveCombos())
                .setComboUsedCount(stats.getComboUsedCount())
                .build());
    }

    @Override
    public void createVoucher(SaveVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        promotionService.createVoucher(toCommand(null, request));
        complete(responseObserver, action("Voucher created"));
    }

    @Override
    public void updateVoucher(UpdateVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        promotionService.updateVoucher(UUID.fromString(request.getId()), toCommand(request.getId(), request.getVoucher()));
        complete(responseObserver, action("Voucher updated"));
    }

    @Override
    public void deleteVoucher(DeleteVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        promotionService.deleteVoucher(UUID.fromString(request.getId()));
        complete(responseObserver, action("Voucher deleted"));
    }

    @Override
    public void publishVoucher(PublishVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        PublishVoucherCommand command = read(request.getPayloadJson(), PublishVoucherCommand.class);
        var result = marketingService.publishVoucher(UUID.fromString(request.getId()), command);
        complete(responseObserver, action("Voucher publish accepted: " + result.acceptedCount()));
    }

    @Override
    public void getMarketingCampaigns(GetMarketingListRequest request, StreamObserver<MarketingJsonResponse> responseObserver) {
        complete(responseObserver, json(marketingService.getCampaigns(blankToNull(request.getQuery()), blankToNull(request.getStatus()))));
    }

    @Override
    public void createMarketingCampaign(MarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.createCampaign(read(request.getPayloadJson(), SaveMarketingCampaignCommand.class));
        complete(responseObserver, action("Campaign saved"));
    }

    @Override
    public void updateMarketingCampaign(UpdateMarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.updateCampaign(UUID.fromString(request.getId()), read(request.getPayloadJson(), SaveMarketingCampaignCommand.class));
        complete(responseObserver, action("Campaign updated"));
    }

    @Override
    public void deleteMarketingCampaign(DeleteMarketingRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.deleteCampaign(UUID.fromString(request.getId()));
        complete(responseObserver, action("Campaign deleted"));
    }

    @Override
    public void getMarketingCombos(GetMarketingListRequest request, StreamObserver<MarketingJsonResponse> responseObserver) {
        complete(responseObserver, json(marketingService.getCombos(blankToNull(request.getQuery()), blankToNull(request.getStatus()))));
    }

    @Override
    public void createMarketingCombo(MarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.createCombo(read(request.getPayloadJson(), SaveMarketingComboCommand.class));
        complete(responseObserver, action("Combo saved"));
    }

    @Override
    public void updateMarketingCombo(UpdateMarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.updateCombo(UUID.fromString(request.getId()), read(request.getPayloadJson(), SaveMarketingComboCommand.class));
        complete(responseObserver, action("Combo updated"));
    }

    @Override
    public void deleteMarketingCombo(DeleteMarketingRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.deleteCombo(UUID.fromString(request.getId()));
        complete(responseObserver, action("Combo deleted"));
    }

    @Override
    public void getMarketingNotifications(GetMarketingListRequest request, StreamObserver<MarketingJsonResponse> responseObserver) {
        complete(responseObserver, json(marketingService.getNotifications(blankToNull(request.getQuery()), blankToNull(request.getStatus()))));
    }

    @Override
    public void createMarketingNotification(MarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.createNotification(read(request.getPayloadJson(), SaveMarketingNotificationCommand.class));
        complete(responseObserver, action("Notification saved"));
    }

    @Override
    public void updateMarketingNotification(UpdateMarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.updateNotification(UUID.fromString(request.getId()), read(request.getPayloadJson(), SaveMarketingNotificationCommand.class));
        complete(responseObserver, action("Notification updated"));
    }

    @Override
    public void deleteMarketingNotification(DeleteMarketingRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        marketingService.deleteNotification(UUID.fromString(request.getId()));
        complete(responseObserver, action("Notification deleted"));
    }

    @Override
    public void validateOrderVouchers(ValidateOrderVouchersRequest request, StreamObserver<ValidateOrderVouchersResponse> responseObserver) {
        var result = promotionService.validateOrderVouchers(ValidateOrderVouchersCommand.builder()
                .userId(parseUuid(request.getUserId()))
                .shopVoucherCode(request.getShopVoucherCode())
                .shippingVoucherCode(request.getShippingVoucherCode())
                .subtotal(request.getSubtotal())
                .shippingFee(request.getShippingFee())
                .build());
        complete(responseObserver, ValidateOrderVouchersResponse.newBuilder()
                .setValid(result.isValid())
                .setMessage(value(result.getMessage()))
                .setDiscountAmount(number(result.getDiscountAmount()))
                .setShippingDiscount(number(result.getShippingDiscount()))
                .build());
    }

    @Override
    public void validateOrderCombo(ValidateOrderComboRequest request, StreamObserver<ValidateOrderComboResponse> responseObserver) {
        ValidateComboCommand command = new ValidateComboCommand();
        command.setUserId(request.getUserId());
        command.setComboId(request.getComboId());
        command.setItems(request.getItemsList().stream().map(item -> {
            ValidateComboCommand.Item commandItem = new ValidateComboCommand.Item();
            commandItem.setProductId(item.getProductId());
            commandItem.setVariantId(item.getVariantId());
            commandItem.setQuantity(item.getQuantity());
            commandItem.setPrice(item.getPrice());
            return commandItem;
        }).toList());
        var result = marketingService.validateCombo(command);
        complete(responseObserver, ValidateOrderComboResponse.newBuilder()
                .setValid(result.isValid())
                .setComboId(value(result.getComboId()))
                .setComboName(value(result.getComboName()))
                .setOriginalAmount(result.getOriginalAmount())
                .setFinalAmount(result.getFinalAmount())
                .setComboDiscount(result.getComboDiscount())
                .setMessage(value(result.getMessage()))
                .build());
    }

    private SavePromotionCommand toCommand(String id, SaveVoucherRequest request) {
        return SavePromotionCommand.builder()
                .id(id)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .voucherType(request.getVoucherType())
                .discountType(request.getDiscountType())
                .discountValue(request.hasDiscountValue() ? request.getDiscountValue() : null)
                .maxDiscount(request.hasMaxDiscount() ? request.getMaxDiscount() : null)
                .minOrder(request.hasMinOrder() ? request.getMinOrder() : null)
                .startDate(parseDate(request.getStartDate()))
                .endDate(parseDate(request.getEndDate()))
                .active(request.hasActive() ? request.getActive() : null)
                .build();
    }

    private MarketingJsonResponse json(MarketingListResponse<?> response) {
        try {
            return MarketingJsonResponse.newBuilder()
                    .setPayloadJson(objectMapper.writeValueAsString(response))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize marketing response", ex);
        }
    }

    private <T> T read(String payloadJson, Class<T> type) {
        try {
            return objectMapper.readValue(value(payloadJson).isBlank() ? "{}" : payloadJson, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid promotion payload", ex);
        }
    }

    private AdminActionResponse action(String message) {
        return AdminActionResponse.newBuilder().setSuccess(true).setMessage(message).build();
    }

    private <T> void complete(StreamObserver<T> observer, T value) {
        observer.onNext(value);
        observer.onCompleted();
    }

    private LocalDateTime parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String format(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private double number(Double value) {
        return value == null ? 0.0 : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
