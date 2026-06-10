package com.furnisight.admin.account.user.web.dto.response;

import java.util.List;

public record UserPageResponse(
        List<UserSummaryResponse> accounts,
        int totalPages,
        long totalElements,
        int currentPage
) {
}
