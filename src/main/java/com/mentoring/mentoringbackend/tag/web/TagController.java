package com.mentoring.mentoringbackend.tag.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.tag.domain.TagType;
import com.mentoring.mentoringbackend.tag.dto.TagCreateRequest;
import com.mentoring.mentoringbackend.tag.dto.TagDto;
import com.mentoring.mentoringbackend.tag.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // 태그 생성 (초기 세팅 or 관리자용)
    @PostMapping
    public ApiResponse<TagDto> createTag(@RequestBody @Valid TagCreateRequest request) {
        return ApiResponse.success(tagService.createTag(request));
    }

    // 학생/멘토/멘티가 자신의 프로필용 커스텀 태그 생성
    @PostMapping("/custom")
    public ApiResponse<TagDto> createCustomTag(@RequestBody @Valid TagCreateRequest request) {
        return ApiResponse.success(tagService.createCustomTag(request));
    }

    // 전체 태그 조회
    @GetMapping
    public ApiResponse<List<TagDto>> getAllTags() {
        return ApiResponse.success(tagService.getAllTags());
    }

    // 타입별 태그 조회
    @GetMapping("/type/{type}")
    public ApiResponse<List<TagDto>> getTagsByType(@PathVariable TagType type) {
        return ApiResponse.success(tagService.getTagsByType(type));
    }

    // 매칭에 사용 가능한 태그만
    @GetMapping("/matchable")
    public ApiResponse<List<TagDto>> getMatchableTags() {
        return ApiResponse.success(tagService.getMatchableTags());
    }
}
