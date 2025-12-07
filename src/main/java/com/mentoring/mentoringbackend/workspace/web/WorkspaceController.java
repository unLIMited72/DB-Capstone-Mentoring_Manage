package com.mentoring.mentoringbackend.workspace.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceCreateRequest;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceDetailResponse;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceSummaryResponse;
import com.mentoring.mentoringbackend.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * Create workspace (admin / operator use)
     */
    @PostMapping
    public ApiResponse<WorkspaceDetailResponse> createWorkspace(
            @RequestBody @Valid WorkspaceCreateRequest request
    ) {
        return ApiResponse.success(workspaceService.createWorkspace(request));
    }

    /**
     * Get workspace detail (only members can access)
     */
    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceDetailResponse> getWorkspace(
            @PathVariable Long workspaceId
    ) {
        return ApiResponse.success(workspaceService.getWorkspace(workspaceId));
    }

    /**
     * Get workspaces that the current user belongs to
     * (mentor / mentee dashboard)
     */
    @GetMapping("/me")
    public ApiResponse<List<WorkspaceSummaryResponse>> getMyWorkspaces() {
        return ApiResponse.success(workspaceService.getMyWorkspaces());
    }

    /**
     * Admin: get all workspaces in the system
     */
    @GetMapping("/admin")
    public ApiResponse<List<WorkspaceSummaryResponse>> getAllWorkspacesForAdmin() {
        return ApiResponse.success(workspaceService.getAllWorkspacesForAdmin());
    }
}
