package com.furnisight.admin.voucher.application;

import com.furnisight.admin.order.VoucherDto;
import com.furnisight.admin.order.VoucherListResponse;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VoucherServiceTest {

    @Test
    void keepsVoucherFallbackStatusLabel() {
        AdminOrderGrpcClient client = mock(AdminOrderGrpcClient.class);
        when(client.getVouchers("", "ACTIVE"))
                .thenReturn(VoucherListResponse.newBuilder()
                        .addVouchers(VoucherDto.newBuilder()
                                .setId("voucher-1")
                                .setCode("SAVE10")
                                .setName("Save")
                                .setActive(true)
                                .build())
                        .build());

        var response = new VoucherService(client).getVouchers("", "ACTIVE");

        assertThat(response.items()).singleElement()
                .extracting(item -> item.statusLabel())
                .isEqualTo("Đang bật");
    }
}
