package com.supportdesk.controller;

import com.supportdesk.dto.DashboardStatsDTO;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        DashboardStatsDTO stats = dashboardService.getDashboardStats(orgId);
        return ResponseEntity.ok(stats);
    }
}

