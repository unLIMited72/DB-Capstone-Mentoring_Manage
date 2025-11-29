package com.mentoring.mentoringbackend.user.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.user.domain.UserTag;
import com.mentoring.mentoringbackend.user.dto.UserTagRequest;
import com.mentoring.mentoringbackend.user.dto.UserTagResponse;
import com.mentoring.mentoringbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/tags")
@RequiredArgsConstructor
public class UserTagController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserTagResponse>> getMyTags() {
        List<UserTag> list = userService.getMyTags();
        List<UserTagResponse> dtoList = list.stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @PutMapping
    public ApiResponse<List<UserTagResponse>> updateMyTags(
            @RequestBody @Valid List<UserTagRequest> requests
    ) {
        List<UserTag> list = userService.updateMyTags(requests);
        List<UserTagResponse> dtoList = list.stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(dtoList);
    }

    private UserTagResponse toResponse(UserTag userTag) {
        return UserTagResponse.builder()
                .id(userTag.getId())
                .tagId(userTag.getTag().getId())
                .tagName(userTag.getTag().getName())
                .tagType(userTag.getTag().getType())
                .relationType(userTag.getRelationType())
                .level(userTag.getLevel())
                .build();
    }
}
