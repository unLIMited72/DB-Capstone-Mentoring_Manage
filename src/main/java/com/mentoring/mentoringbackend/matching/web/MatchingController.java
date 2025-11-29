package com.mentoring.mentoringbackend.matching.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.matching.dto.MatchingConfigRequest;
import com.mentoring.mentoringbackend.matching.dto.MatchingSuggestionResponse;
import com.mentoring.mentoringbackend.matching.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs/{programId}/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 멘티 입장에서,
     * 해당 프로그램에서 본인에게 맞는 멘토 모집글 추천
     *
     * GET /api/programs/{programId}/matching/recommendations
     */
    @GetMapping("/recommendations")
    public ApiResponse<List<MatchingSuggestionResponse>> recommendMentorPosts(
            @PathVariable Long programId
    ) {
        return ApiResponse.success(matchingService.recommendMentorPosts(programId));
    }

    /**
     * 매칭 가중치 설정 조회 (관리자 화면 등에서 사용)
     *
     * GET /api/programs/{programId}/matching/config
     */
    @GetMapping("/config")
    public ApiResponse<MatchingConfigRequest> getConfig(
            @PathVariable Long programId
    ) {
        return ApiResponse.success(matchingService.getConfig(programId));
    }

    /**
     * 매칭 가중치 설정 저장/수정
     *
     * PUT /api/programs/{programId}/matching/config
     */
    @PutMapping("/config")
    public ApiResponse<MatchingConfigRequest> updateConfig(
            @PathVariable Long programId,
            @RequestBody @Valid MatchingConfigRequest request
    ) {
        return ApiResponse.success(matchingService.updateConfig(programId, request));
    }
}
