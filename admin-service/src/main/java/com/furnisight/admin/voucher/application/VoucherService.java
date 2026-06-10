package com.furnisight.admin.voucher.application;

import com.furnisight.admin.order.AdminActionResponse;
import com.furnisight.admin.order.CreateVoucherRequest;
import com.furnisight.admin.order.UpdateVoucherRequest;
import com.furnisight.admin.order.VoucherDto;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.shared.web.ActionResultResponse;
import com.furnisight.admin.voucher.web.dto.request.UpsertVoucherRequest;
import com.furnisight.admin.voucher.web.dto.response.VoucherListResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final AdminOrderGrpcClient orderClient;

    public VoucherListResponse getVouchers(String query, String status) {
        return new VoucherListResponse(orderClient.getVouchers(query, status).getVouchersList().stream()
                .map(this::toResponse)
                .toList());
    }

    public ActionResultResponse createVoucher(UpsertVoucherRequest request) {
        return toActionResult(orderClient.createVoucher(CreateVoucherRequest.newBuilder()
                .setCode(value(request.code())).setName(value(request.name()))
                .setDescription(value(request.description())).setIcon(value(request.icon()))
                .setDiscountType(value(request.discountType())).setDiscountValue(request.discountValue())
                .setMaxDiscount(request.maxDiscount()).setMinOrder(request.minOrder())
                .setStartDate(value(request.startDate())).setEndDate(value(request.endDate()))
                .setActive(request.active()).build()));
    }

    public ActionResultResponse updateVoucher(String id, UpsertVoucherRequest request) {
        return toActionResult(orderClient.updateVoucher(UpdateVoucherRequest.newBuilder()
                .setId(value(id)).setCode(value(request.code())).setName(value(request.name()))
                .setDescription(value(request.description())).setIcon(value(request.icon()))
                .setDiscountType(value(request.discountType())).setDiscountValue(request.discountValue())
                .setMaxDiscount(request.maxDiscount()).setMinOrder(request.minOrder())
                .setStartDate(value(request.startDate())).setEndDate(value(request.endDate()))
                .setActive(request.active()).build()));
    }

    public ActionResultResponse deleteVoucher(String id) {
        return toActionResult(orderClient.deleteVoucher(id));
    }

    private VoucherResponse toResponse(VoucherDto voucher) {
        return new VoucherResponse(
                voucher.getId(), voucher.getCode(), voucher.getName(), voucher.getDescription(),
                voucher.getIcon(), voucher.getDiscountType(), voucher.getDiscountValue(),
                voucher.getMaxDiscount(), voucher.getMinOrder(), voucher.getStartDate(),
                voucher.getEndDate(), voucher.getActive(),
                voucher.getStatusLabel().isBlank()
                        ? voucherStatusLabel(voucher.getActive(), voucher.getEndDate())
                        : voucher.getStatusLabel());
    }

    private String voucherStatusLabel(boolean active, String endDate) {
        if (!active) {
            return "Đã tắt";
        }
        if (endDate != null && !endDate.isBlank()) {
            try {
                if (LocalDateTime.parse(endDate).isBefore(LocalDateTime.now())) {
                    return "Hết hạn";
                }
            } catch (Exception ignored) {
                return "Đang bật";
            }
        }
        return "Đang bật";
    }

    private ActionResultResponse toActionResult(AdminActionResponse response) {
        return new ActionResultResponse(response.getSuccess(), response.getMessage());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
