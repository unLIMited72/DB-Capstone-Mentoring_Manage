package com.mentoring.mentoringbackend.feedback.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.feedback.dto.FeedbackRequest;
import com.mentoring.mentoringbackend.feedback.dto.FeedbackResponse;
import com.mentoring.mentoringbackend.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ApiResponse<FeedbackResponse> createFeedback(
            @PathVariable Long workspaceId,
            @RequestBody @Valid FeedbackRequest request
    ) {
        return ApiResponse.success(feedbackService.createFeedback(workspaceId, request));
    }

    /**
     * 전체 워크스페이스 피드백 조회
     * 특정 세션만 보고 싶으면 /api/workspaces/{workspaceId}/feedbacks?sessionId=1
     */
    @GetMapping
    public ApiResponse<List<FeedbackResponse>> getFeedbacks(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Long sessionId
    ) {
        return ApiResponse.success(feedbackService.getFeedbacks(workspaceId, sessionId));
    }
}
