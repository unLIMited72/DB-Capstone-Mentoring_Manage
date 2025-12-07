package com.mentoring.mentoringbackend.workspace.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.common.exception.CustomException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceCreateRequest;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceDetailResponse;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceSummaryResponse;
import com.mentoring.mentoringbackend.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// WorkspaceController.java

import com.mentoring.mentoringbackend.auth.SecurityUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    // 수동 생성 (관리자/운영자용)
    @PostMapping
    public ApiResponse<WorkspaceDetailResponse> createWorkspace(
            @RequestBody @Valid WorkspaceCreateRequest request
    ) {
        return ApiResponse.success(workspaceService.createWorkspace(request));
    }

    // 상세 조회
    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceDetailResponse> getWorkspace(
            @PathVariable Long workspaceId
    ) {
        return ApiResponse.success(workspaceService.getWorkspace(workspaceId));
    }

    // 내가 속한 워크스페이스 목록
    @GetMapping("/me")
    public ApiResponse<List<WorkspaceSummaryResponse>> getMyWorkspaces(
            @AuthenticationPrincipal SecurityUserDetails principal
)   {
        if (principal == null) {
            // UserController ���� ���� �Ͱ� ������ ���� ���� ���
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        Long userId = principal.getId();   // SecurityUserDetails 안에 있는 id 꺼내기

        return ApiResponse.success(workspaceService.getMyWorkspaces(userId));
}
}
