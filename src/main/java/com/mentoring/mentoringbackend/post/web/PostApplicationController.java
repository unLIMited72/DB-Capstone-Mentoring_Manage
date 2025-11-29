package com.mentoring.mentoringbackend.post.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.post.dto.PostApplicationRequest;
import com.mentoring.mentoringbackend.post.dto.PostApplicationResponse;
import com.mentoring.mentoringbackend.post.service.PostApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-applications")
@RequiredArgsConstructor
public class PostApplicationController {

    private final PostApplicationService postApplicationService;

    // 신청 보내기
    @PostMapping
    public ApiResponse<PostApplicationResponse> apply(
            @RequestBody @Valid PostApplicationRequest request
    ) {
        return ApiResponse.success(postApplicationService.apply(request));
    }

    // 내가 보낸 신청
    @GetMapping("/me/sent")
    public ApiResponse<List<PostApplicationResponse>> getMySent() {
        return ApiResponse.success(postApplicationService.getMySentApplications());
    }

    // 내가 받은 신청 (내 글에 온 신청)
    @GetMapping("/me/received")
    public ApiResponse<List<PostApplicationResponse>> getMyReceived() {
        return ApiResponse.success(postApplicationService.getApplicationsToMe());
    }

    // 신청 수락
    @PostMapping("/{applicationId}/accept")
    public ApiResponse<PostApplicationResponse> accept(
            @PathVariable Long applicationId
    ) {
        return ApiResponse.success(postApplicationService.accept(applicationId));
    }

    // 신청 거절
    @PostMapping("/{applicationId}/reject")
    public ApiResponse<PostApplicationResponse> reject(
            @PathVariable Long applicationId
    ) {
        return ApiResponse.success(postApplicationService.reject(applicationId));
    }
}
