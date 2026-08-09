package com.furnisight.promotion.adapter.in.grpc;

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
import com.furnisight.promotion.domain.common.PageResponse;
import com.furnisight.promotion.application.dto.PublishVoucherCommand;
import com.furnisight.promotion.application.dto.SaveMarketingCampaignCommand;
import com.furnisight.promotion.application.dto.SaveMarketingComboCommand;
import com.furnisight.promotion.application.dto.SaveMarketingNotificationCommand;
import com.furnisight.promotion.application.dto.SavePromotionCommand;
import com.furnisight.promotion.application.dto.ValidateComboCommand;
import com.furnisight.promotion.application.dto.ValidateOrderVouchersCommand;
import com.furnisight.promotion.application.dto.VoucherStatsResponse;
// Marketing Service extracted to UseCases
import com.furnisight.promotion.application.port.in.usecase.*;
import com.furnisight.promotion.application.port.in.query.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class AdminPromotionGrpcService extends AdminPromotionServiceGrpc.AdminPromotionServiceImplBase {

    private final GetAdminVouchersUseCase getAdminVouchersUseCase;
    private final GetPromotionStatsUseCase getPromotionStatsUseCase;
    private final CreateVoucherUseCase createVoucherUseCase;
    private final UpdateVoucherUseCase updateVoucherUseCase;
    private final DeleteVoucherUseCase deleteVoucherUseCase;
    private final PublishVoucherUseCase publishVoucherUseCase;
    private final GetCampaignsUseCase getCampaignsUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;
    private final UpdateCampaignUseCase updateCampaignUseCase;
    private final DeleteCampaignUseCase deleteCampaignUseCase;
    private final GetCombosUseCase getCombosUseCase;
    private final CreateComboUseCase createComboUseCase;
    private final UpdateComboUseCase updateComboUseCase;
    private final DeleteComboUseCase deleteComboUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final UpdateNotificationUseCase updateNotificationUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;
    private final ValidateOrderVouchersUseCase validateOrderVouchersUseCase;
    private final ValidateComboUseCase validateComboUseCase;
    
    private final AdminPromotionGrpcMapper mapper;

    @Override
    public void getAdminVouchers(GetAdminVouchersRequest request, StreamObserver<VoucherListResponse> responseObserver) {
        VoucherListResponse.Builder response = VoucherListResponse.newBuilder();
        getAdminVouchersUseCase.getAdminVouchers(GetAdminVouchersQuery.builder().query(request.getQuery()).type(request.getType()).status(request.getStatus()).build())
                .forEach(voucher -> {
                    VoucherDto.Builder builder = VoucherDto.newBuilder()
                            .setId(mapper.value(voucher.getId()))
                            .setCode(mapper.value(voucher.getCode()))
                            .setName(mapper.value(voucher.getName()))
                            .setDescription(mapper.value(voucher.getDescription()))
                            .setIcon(mapper.value(voucher.getIcon()))
                            .setVoucherType(mapper.value(voucher.getVoucherType()))
                            .setDiscountType(mapper.value(voucher.getDiscountType()))
                            .setDiscountValue(mapper.number(voucher.getDiscountValue()))
                            .setStartDate(mapper.format(voucher.getStartDate()))
                            .setEndDate(mapper.format(voucher.getEndDate()))
                            .setActive(voucher.isActive())
                            .setStatusLabel(mapper.value(voucher.getStatusLabel()))
                            .setIssuedCount(voucher.getIssuedCount());
                    if (voucher.getMaxDiscount() != null) {
                        builder.setMaxDiscount(voucher.getMaxDiscount());
                    }
                    if (voucher.getMinOrder() != null) {
                        builder.setMinOrder(voucher.getMinOrder());
                    }
                    response.addItems(builder.build());
                });
        mapper.complete(responseObserver, response.build());
    }

    @Override
    public void getVoucherStats(Empty request, StreamObserver<com.furnisight.admin.promotion.VoucherStatsResponse> responseObserver) {
        VoucherStatsResponse stats = getPromotionStatsUseCase.getStats(new GetPromotionStatsQuery());
        mapper.complete(responseObserver, com.furnisight.admin.promotion.VoucherStatsResponse.newBuilder()
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
        createVoucherUseCase.createVoucher(mapper.toCommand(null, request));
        mapper.complete(responseObserver, mapper.action("Voucher created"));
    }

    @Override
    public void updateVoucher(UpdateVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        updateVoucherUseCase.updateVoucher(UpdateVoucherQuery.builder().id(UUID.fromString(request.getId())).command(mapper.toCommand(request.getId(), request.getVoucher())).build());
        mapper.complete(responseObserver, mapper.action("Voucher updated"));
    }

    @Override
    public void deleteVoucher(DeleteVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        deleteVoucherUseCase.deleteVoucher(DeleteVoucherQuery.builder().id(UUID.fromString(request.getId())).build());
        mapper.complete(responseObserver, mapper.action("Voucher deleted"));
    }

    @Override
    public void publishVoucher(PublishVoucherRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        PublishVoucherCommand command = mapper.read(request.getPayloadJson(), PublishVoucherCommand.class);
        var result = publishVoucherUseCase.publishVoucher(PublishVoucherQuery.builder().voucherId(UUID.fromString(request.getId())).command(command).build());
        mapper.complete(responseObserver, mapper.action("Voucher publish accepted: " + result.acceptedCount()));
    }

    @Override
    public void getMarketingCampaigns(GetMarketingListRequest request, StreamObserver<MarketingJsonResponse> responseObserver) {
        mapper.complete(responseObserver, mapper.json(getCampaignsUseCase.getCampaigns(GetCampaignsQuery.builder().query(request.getQuery()).status(request.getStatus()).build())));
    }

    @Override
    public void createMarketingCampaign(MarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        createCampaignUseCase.createCampaign(mapper.read(request.getPayloadJson(), SaveMarketingCampaignCommand.class));
        mapper.complete(responseObserver, mapper.action("Campaign saved"));
    }

    @Override
    public void updateMarketingCampaign(UpdateMarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        updateCampaignUseCase.updateCampaign(UpdateCampaignQuery.builder().id(UUID.fromString(request.getId())).command(mapper.read(request.getPayloadJson(), SaveMarketingCampaignCommand.class)).build());
        mapper.complete(responseObserver, mapper.action("Campaign updated"));
    }

    @Override
    public void deleteMarketingCampaign(DeleteMarketingRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        deleteCampaignUseCase.deleteCampaign(DeleteCampaignQuery.builder().id(UUID.fromString(request.getId())).build());
        mapper.complete(responseObserver, mapper.action("Campaign deleted"));
    }

    @Override
    public void getMarketingCombos(GetMarketingListRequest request, StreamObserver<MarketingJsonResponse> responseObserver) {
        mapper.complete(responseObserver, mapper.json(getCombosUseCase.getCombos(GetCombosQuery.builder().query(request.getQuery()).status(request.getStatus()).build())));
    }

    @Override
    public void createMarketingCombo(MarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        createComboUseCase.createCombo(mapper.read(request.getPayloadJson(), SaveMarketingComboCommand.class));
        mapper.complete(responseObserver, mapper.action("Combo saved"));
    }

    @Override
    public void updateMarketingCombo(UpdateMarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        updateComboUseCase.updateCombo(UpdateComboQuery.builder().id(UUID.fromString(request.getId())).command(mapper.read(request.getPayloadJson(), SaveMarketingComboCommand.class)).build());
        mapper.complete(responseObserver, mapper.action("Combo updated"));
    }

    @Override
    public void deleteMarketingCombo(DeleteMarketingRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        deleteComboUseCase.deleteCombo(DeleteComboQuery.builder().id(UUID.fromString(request.getId())).build());
        mapper.complete(responseObserver, mapper.action("Combo deleted"));
    }

    @Override
    public void getMarketingNotifications(GetMarketingListRequest request, StreamObserver<MarketingJsonResponse> responseObserver) {
        mapper.complete(responseObserver, mapper.json(getNotificationsUseCase.getNotifications(GetNotificationsQuery.builder().query(request.getQuery()).status(request.getStatus()).build())));
    }

    @Override
    public void createMarketingNotification(MarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        createNotificationUseCase.createNotification(mapper.read(request.getPayloadJson(), SaveMarketingNotificationCommand.class));
        mapper.complete(responseObserver, mapper.action("Notification saved"));
    }

    @Override
    public void updateMarketingNotification(UpdateMarketingJsonRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        updateNotificationUseCase.updateNotification(UpdateNotificationQuery.builder().id(UUID.fromString(request.getId())).command(mapper.read(request.getPayloadJson(), SaveMarketingNotificationCommand.class)).build());
        mapper.complete(responseObserver, mapper.action("Notification updated"));
    }

    @Override
    public void deleteMarketingNotification(DeleteMarketingRequest request, StreamObserver<AdminActionResponse> responseObserver) {
        deleteNotificationUseCase.deleteNotification(DeleteNotificationQuery.builder().id(UUID.fromString(request.getId())).build());
        mapper.complete(responseObserver, mapper.action("Notification deleted"));
    }

    @Override
    public void validateOrderVouchers(ValidateOrderVouchersRequest request, StreamObserver<ValidateOrderVouchersResponse> responseObserver) {
        var result = validateOrderVouchersUseCase.validateOrderVouchers(ValidateOrderVouchersCommand.builder()
                .userId(mapper.parseUuid(request.getUserId()))
                .shopVoucherCode(request.getShopVoucherCode())
                .shippingVoucherCode(request.getShippingVoucherCode())
                .subtotal(request.getSubtotal())
                .shippingFee(request.getShippingFee())
                .build());
        mapper.complete(responseObserver, ValidateOrderVouchersResponse.newBuilder()
                .setValid(result.isValid())
                .setMessage(mapper.value(result.getMessage()))
                .setDiscountAmount(mapper.number(result.getDiscountAmount()))
                .setShippingDiscount(mapper.number(result.getShippingDiscount()))
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
        var result = validateComboUseCase.validateCombo(command);
        mapper.complete(responseObserver, ValidateOrderComboResponse.newBuilder()
                .setValid(result.isValid())
                .setComboId(mapper.value(result.getComboId()))
                .setComboName(mapper.value(result.getComboName()))
                .setOriginalAmount(result.getOriginalAmount())
                .setFinalAmount(result.getFinalAmount())
                .setComboDiscount(result.getComboDiscount())
                .setMessage(mapper.value(result.getMessage()))
                .build());
    }
}
