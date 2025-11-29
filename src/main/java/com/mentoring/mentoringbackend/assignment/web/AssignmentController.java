package com.mentoring.mentoringbackend.assignment.web;

import com.mentoring.mentoringbackend.assignment.dto.AssignmentCreateRequest;
import com.mentoring.mentoringbackend.assignment.dto.AssignmentResponse;
import com.mentoring.mentoringbackend.assignment.service.AssignmentService;
import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ApiResponse<AssignmentResponse> createAssignment(
            @PathVariable Long workspaceId,
            @RequestBody @Valid AssignmentCreateRequest request
    ) {
        return ApiResponse.success(assignmentService.createAssignment(workspaceId, request));
    }

    @GetMapping
    public ApiResponse<List<AssignmentResponse>> getAssignments(
            @PathVariable Long workspaceId
    ) {
        return ApiResponse.success(assignmentService.getAssignments(workspaceId));
    }

    @GetMapping("/{assignmentId}")
    public ApiResponse<AssignmentResponse> getAssignment(
            @PathVariable Long workspaceId,
            @PathVariable Long assignmentId
    ) {
        return ApiResponse.success(assignmentService.getAssignment(workspaceId, assignmentId));
    }

    @PutMapping("/{assignmentId}")
    public ApiResponse<AssignmentResponse> updateAssignment(
            @PathVariable Long workspaceId,
            @PathVariable Long assignmentId,
            @RequestBody @Valid AssignmentCreateRequest request
    ) {
        return ApiResponse.success(assignmentService.updateAssignment(workspaceId, assignmentId, request));
    }
}
