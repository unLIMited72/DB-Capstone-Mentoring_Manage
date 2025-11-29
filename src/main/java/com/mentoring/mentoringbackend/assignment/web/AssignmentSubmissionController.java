package com.mentoring.mentoringbackend.assignment.web;

import com.mentoring.mentoringbackend.assignment.dto.AssignmentSubmissionRequest;
import com.mentoring.mentoringbackend.assignment.dto.AssignmentSubmissionResponse;
import com.mentoring.mentoringbackend.assignment.service.AssignmentSubmissionService;
import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService assignmentSubmissionService;

    @GetMapping
    public ApiResponse<List<AssignmentSubmissionResponse>> getSubmissions(
            @PathVariable Long assignmentId
    ) {
        return ApiResponse.success(assignmentSubmissionService.getSubmissions(assignmentId));
    }

    @PostMapping
    public ApiResponse<AssignmentSubmissionResponse> submitOrUpdate(
            @PathVariable Long assignmentId,
            @RequestBody @Valid AssignmentSubmissionRequest request
    ) {
        return ApiResponse.success(assignmentSubmissionService.submitOrUpdate(assignmentId, request));
    }
}
