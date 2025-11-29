package com.mentoring.mentoringbackend.session.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.session.dto.SessionCreateRequest;
import com.mentoring.mentoringbackend.session.dto.SessionResponse;
import com.mentoring.mentoringbackend.session.dto.SessionUpdateRequest;
import com.mentoring.mentoringbackend.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // 세션 생성
    @PostMapping
    public ApiResponse<SessionResponse> createSession(
            @PathVariable Long workspaceId,
            @RequestBody @Valid SessionCreateRequest request
    ) {
        return ApiResponse.success(sessionService.createSession(workspaceId, request));
    }

    // 특정 워크스페이스의 세션 목록
    @GetMapping
    public ApiResponse<List<SessionResponse>> getSessions(
            @PathVariable Long workspaceId
    ) {
        return ApiResponse.success(sessionService.getSessionsByWorkspace(workspaceId));
    }

    // 세션 상세
    @GetMapping("/{sessionId}")
    public ApiResponse<SessionResponse> getSession(
            @PathVariable Long workspaceId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(sessionService.getSession(workspaceId, sessionId));
    }

    // 세션 수정
    @PutMapping("/{sessionId}")
    public ApiResponse<SessionResponse> updateSession(
            @PathVariable Long workspaceId,
            @PathVariable Long sessionId,
            @RequestBody @Valid SessionUpdateRequest request
    ) {
        return ApiResponse.success(sessionService.updateSession(workspaceId, sessionId, request));
    }
}
