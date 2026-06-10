package com.furnisight.admin.stats.web;

import com.furnisight.admin.stats.web.dto.response.StatsResponse;
import com.furnisight.admin.stats.application.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService adminStatsService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTS') or hasAuthority('reports') or hasAuthority('dashboard') or hasAuthority('MANAGE_USERS')")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }
}
