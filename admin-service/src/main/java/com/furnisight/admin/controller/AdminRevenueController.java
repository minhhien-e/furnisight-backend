package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminRevenueResponse;
import com.furnisight.admin.service.AdminRevenueService;
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
public class AdminRevenueController {

    private final AdminRevenueService adminRevenueService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('PRODUCT_VIEW') or hasAuthority('product_view')")
    public ResponseEntity<AdminRevenueResponse> getRevenueSummary() {
        return ResponseEntity.ok(adminRevenueService.getRevenueSummary());
    }
}
