package com.furnisight.admin.controller.dto;

import java.util.List;

public record AdminAccountPageResponse(
        List<AdminAccountSummaryResponse> accounts,
        int totalPages,
        long totalElements,
        int currentPage
) {
}
