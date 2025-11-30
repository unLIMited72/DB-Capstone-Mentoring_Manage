package com.mentoring.mentoringbackend.admin;

import com.mentoring.mentoringbackend.admin.AdminDashboardService.DashboardSummary;
import com.mentoring.mentoringbackend.admin.AdminDashboardService.KpiSummary;
import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 관리자용 대시보드 요약 정보 (단순 카운트)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummary>> getDashboard() {
        DashboardSummary summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * KPI 지표 요약
     */
    @GetMapping("/kpi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<KpiSummary>> getKpi() {
        KpiSummary kpi = adminDashboardService.getKpiSummary();
        return ResponseEntity.ok(ApiResponse.success(kpi));
    }
}
