package com.furnisight.admin.voucher.application;

import com.furnisight.admin.voucher.infrastructure.PromotionAdminClient;
import com.furnisight.admin.voucher.web.dto.response.VoucherListResponse;
import com.furnisight.admin.voucher.web.dto.response.VoucherResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VoucherServiceTest {

    @Test
    void forwardsVoucherListFromPromotionService() {
        PromotionAdminClient client = mock(PromotionAdminClient.class);
        when(client.getVouchers("", null, "ACTIVE"))
                .thenReturn(new VoucherListResponse(List.of(new VoucherResponse(
                        "voucher-1",
                        "SAVE10",
                        "Save",
                        null,
                        null,
                        "PUBLIC",
                        "PERCENT",
                        10.0,
                        null,
                        0.0,
                        null,
                        null,
                        true,
                        "Dang bat",
                        0
                ))));

        var response = new VoucherService(client).getVouchers("", null, "ACTIVE");

        assertThat(response.items()).singleElement()
                .extracting(VoucherResponse::code)
                .isEqualTo("SAVE10");
    }
}
