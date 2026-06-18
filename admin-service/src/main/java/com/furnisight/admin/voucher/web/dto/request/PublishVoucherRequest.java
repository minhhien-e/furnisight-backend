package com.furnisight.admin.voucher.web.dto.request;

import java.util.List;

public record PublishVoucherRequest(
        String targetType,
        List<String> targetUserIds,
        String segmentKey,
        List<String> channels,
        String title,
        String body
) {
}
