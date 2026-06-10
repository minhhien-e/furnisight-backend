package com.furnisight.admin.revenue.web;

import com.furnisight.admin.revenue.web.dto.response.RevenueResponse;
import com.furnisight.admin.revenue.application.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint doanh thu riêng biệt — không gộp vào dashboard.
 * Sử dụng cơ chế snapshot để load nhanh, tự động refresh khi stale.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService adminRevenueService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view')")
    public ResponseEntity<RevenueResponse> getRevenueSummary() {
        return ResponseEntity.ok(adminRevenueService.getRevenueSummary());
    }
}
