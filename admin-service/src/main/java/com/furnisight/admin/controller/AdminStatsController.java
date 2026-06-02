package com.furnisight.admin.controller;

import com.furnisight.admin.controller.dto.AdminStatsResponse;
import com.furnisight.admin.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTS') or hasAuthority('reports') or hasAuthority('dashboard') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }
}
