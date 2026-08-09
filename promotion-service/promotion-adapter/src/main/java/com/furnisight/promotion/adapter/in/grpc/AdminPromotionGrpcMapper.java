package com.furnisight.promotion.adapter.in.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.admin.promotion.AdminActionResponse;
import com.furnisight.admin.promotion.MarketingJsonResponse;
import com.furnisight.admin.promotion.SaveVoucherRequest;
import com.furnisight.admin.promotion.VoucherDto;
import com.furnisight.promotion.domain.common.PageResponse;
import com.furnisight.promotion.application.dto.PromotionDto;
import com.furnisight.promotion.application.dto.SavePromotionCommand;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminPromotionGrpcMapper {

    private final ObjectMapper objectMapper;

    public VoucherDto toVoucherDto(PromotionDto voucher) {
        VoucherDto.Builder builder = VoucherDto.newBuilder()
                .setId(voucher.getId() != null ? voucher.getId().toString() : "")
                .setCode(voucher.getCode())
                .setName(voucher.getName())
                .setDescription(voucher.getDescription())
                .setIcon(voucher.getIcon())
                .setVoucherType(voucher.getVoucherType())
                .setDiscountType(voucher.getDiscountType())
                .setDiscountValue(voucher.getDiscountValue())
                .setStartDate(format(voucher.getStartDate()))
                .setEndDate(format(voucher.getEndDate()))
                .setActive(voucher.isActive())
                .setStatusLabel(voucher.getStatusLabel())
                .setIssuedCount(voucher.getIssuedCount());
        if (voucher.getMaxDiscount() != null) {
            builder.setMaxDiscount(voucher.getMaxDiscount());
        }
        if (voucher.getMinOrder() != null) {
            builder.setMinOrder(voucher.getMinOrder());
        }
        return builder.build();
    }

    public SavePromotionCommand toCommand(String id, SaveVoucherRequest request) {
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

    public MarketingJsonResponse json(PageResponse<?> response) {
        try {
            return MarketingJsonResponse.newBuilder()
                    .setPayloadJson(objectMapper.writeValueAsString(response))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize marketing response", ex);
        }
    }

    public <T> T read(String payloadJson, Class<T> type) {
        try {
            return objectMapper.readValue(value(payloadJson).isBlank() ? "{}" : payloadJson, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid promotion payload", ex);
        }
    }

    public AdminActionResponse action(String message) {
        return AdminActionResponse.newBuilder().setSuccess(true).setMessage(message).build();
    }

    public <T> void complete(StreamObserver<T> observer, T value) {
        observer.onNext(value);
        observer.onCompleted();
    }

    public LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    public UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    public String format(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public String value(String value) {
        return value == null ? "" : value;
    }

    public double number(Double value) {
        return value == null ? 0.0 : value;
    }

    public String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
