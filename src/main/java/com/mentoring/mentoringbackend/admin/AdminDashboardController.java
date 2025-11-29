package com.mentoring.mentoringbackend.admin;

import com.mentoring.mentoringbackend.admin.AdminDashboardService.DashboardSummary;
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
     * 관리자용 대시보드 요약 정보
     * - 총 사용자 수, 게시글 수, 워크스페이스 수 등
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // ADMIN만 접근
    public ResponseEntity<ApiResponse<DashboardSummary>> getDashboard() {
        DashboardSummary summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
