package com.mentoring.mentoringbackend.user.web;

import com.mentoring.mentoringbackend.auth.SecurityUserDetails;
import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.common.dto.PageResponse;
import com.mentoring.mentoringbackend.common.exception.CustomException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.user.dto.UserProfileResponse;
import com.mentoring.mentoringbackend.user.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSearchService userSearchService;

    @GetMapping
    public ApiResponse<PageResponse<UserProfileResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(userSearchService.listUsers(page, size));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMe(
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        if (principal == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        UserProfileResponse profile = userSearchService.getUserProfile(principal.getId());
        return ApiResponse.success(profile);
    }
}
