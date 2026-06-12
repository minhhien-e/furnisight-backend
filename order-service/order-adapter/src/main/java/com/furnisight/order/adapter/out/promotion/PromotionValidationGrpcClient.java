package com.furnisight.order.adapter.out.promotion;

import com.furnisight.admin.promotion.AdminPromotionServiceGrpc;
import com.furnisight.admin.promotion.ValidateOrderComboItem;
import com.furnisight.admin.promotion.ValidateOrderComboRequest;
import com.furnisight.order.application.promotion.port.out.PromotionValidationPort;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateComboResult;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersRequest;
import com.furnisight.order.application.promotion.port.out.dto.ValidateOrderVouchersResult;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class PromotionValidationGrpcClient implements PromotionValidationPort {

    @GrpcClient("promotion-service")
    private AdminPromotionServiceGrpc.AdminPromotionServiceBlockingStub promotionStub;

    @Override
    public ValidateOrderVouchersResult validateOrderVouchers(ValidateOrderVouchersRequest request) {
        var response = promotionStub.validateOrderVouchers(com.furnisight.admin.promotion.ValidateOrderVouchersRequest.newBuilder()
                .setUserId(request.getUserId() == null ? "" : request.getUserId().toString())
                .setShopVoucherCode(value(request.getShopVoucherCode()))
                .setShippingVoucherCode(value(request.getShippingVoucherCode()))
                .setSubtotal(number(request.getSubtotal()))
                .setShippingFee(number(request.getShippingFee()))
                .build());
        ValidateOrderVouchersResult result = new ValidateOrderVouchersResult();
        result.setValid(response.getValid());
        result.setMessage(response.getMessage());
        result.setDiscountAmount(response.getDiscountAmount());
        result.setShippingDiscount(response.getShippingDiscount());
        return result;
    }

    @Override
    public ValidateComboResult validateCombo(ValidateComboRequest request) {
        ValidateOrderComboRequest.Builder builder = ValidateOrderComboRequest.newBuilder()
                .setUserId(request.getUserId() == null ? "" : request.getUserId().toString())
                .setComboId(value(request.getComboId()));
        if (request.getItems() != null) {
            request.getItems().forEach(item -> builder.addItems(ValidateOrderComboItem.newBuilder()
                    .setProductId(value(item.getProductId()))
                    .setVariantId(value(item.getVariantId()))
                    .setQuantity(item.getQuantity() == null ? 0 : item.getQuantity())
                    .setPrice(number(item.getPrice()))
                    .build()));
        }
        var response = promotionStub.validateOrderCombo(builder.build());
        ValidateComboResult result = new ValidateComboResult();
        result.setValid(response.getValid());
        result.setComboId(response.getComboId());
        result.setComboName(response.getComboName());
        result.setOriginalAmount(response.getOriginalAmount());
        result.setFinalAmount(response.getFinalAmount());
        result.setComboDiscount(response.getComboDiscount());
        result.setMessage(response.getMessage());
        return result;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private double number(Double value) {
        return value == null ? 0.0 : value;
    }
}
