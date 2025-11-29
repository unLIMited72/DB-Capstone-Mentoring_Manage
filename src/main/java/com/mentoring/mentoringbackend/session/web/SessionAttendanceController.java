package com.mentoring.mentoringbackend.session.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.session.dto.SessionAttendanceRequest;
import com.mentoring.mentoringbackend.session.service.SessionAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions/{sessionId}/attendance")
@RequiredArgsConstructor
public class SessionAttendanceController {

    private final SessionAttendanceService sessionAttendanceService;

    // 해당 세션의 출석 현황 조회
    @GetMapping
    public ApiResponse<List<SessionAttendanceRequest>> getAttendance(
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(sessionAttendanceService.getAttendance(sessionId));
    }

    // 한 명의 출석 상태 기록/수정
    @PostMapping
    public ApiResponse<Void> markAttendance(
            @PathVariable Long sessionId,
            @RequestBody @Valid SessionAttendanceRequest request
    ) {
        sessionAttendanceService.markAttendance(sessionId, request);
        return ApiResponse.success(null);
    }
}
