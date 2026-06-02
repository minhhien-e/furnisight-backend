package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminDashboardResponse;
import com.furnisight.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_DASHBOARD') or hasAuthority('dashboard') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminDashboardResponse> getDashboardData() {
        return ResponseEntity.ok(adminDashboardService.getDashboardData());
    }
}
