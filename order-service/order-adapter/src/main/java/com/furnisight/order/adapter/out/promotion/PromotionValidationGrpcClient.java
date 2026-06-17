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
        var builder = com.furnisight.admin.promotion.ValidateOrderVouchersRequest.newBuilder();
        if (request.getUserId() != null) {
            builder.setUserId(request.getUserId().toString());
        }
        if (request.getShopVoucherCode() != null) {
            builder.setShopVoucherCode(request.getShopVoucherCode());
        }
        if (request.getShippingVoucherCode() != null) {
            builder.setShippingVoucherCode(request.getShippingVoucherCode());
        }
        if (request.getSubtotal() != null) {
            builder.setSubtotal(request.getSubtotal());
        }
        if (request.getShippingFee() != null) {
            builder.setShippingFee(request.getShippingFee());
        }
        var response = promotionStub.validateOrderVouchers(builder.build());
        ValidateOrderVouchersResult result = new ValidateOrderVouchersResult();
        result.setValid(response.getValid());
        result.setMessage(response.getMessage());
        result.setDiscountAmount(response.getDiscountAmount());
        result.setShippingDiscount(response.getShippingDiscount());
        return result;
    }

    @Override
    public ValidateComboResult validateCombo(ValidateComboRequest request) {
        ValidateOrderComboRequest.Builder builder = ValidateOrderComboRequest.newBuilder();
        if (request.getUserId() != null) {
            builder.setUserId(request.getUserId().toString());
        }
        if (request.getComboId() != null) {
            builder.setComboId(request.getComboId());
        }
        if (request.getItems() != null) {
            request.getItems().forEach(item -> {
                ValidateOrderComboItem.Builder itemBuilder = ValidateOrderComboItem.newBuilder();
                if (item.getProductId() != null) {
                    itemBuilder.setProductId(item.getProductId());
                }
                if (item.getVariantId() != null) {
                    itemBuilder.setVariantId(item.getVariantId());
                }
                if (item.getQuantity() != null) {
                    itemBuilder.setQuantity(item.getQuantity());
                }
                if (item.getPrice() != null) {
                    itemBuilder.setPrice(item.getPrice());
                }
                builder.addItems(itemBuilder.build());
            });
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
}
