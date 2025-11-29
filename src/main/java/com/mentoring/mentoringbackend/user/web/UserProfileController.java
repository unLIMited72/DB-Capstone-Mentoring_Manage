package com.mentoring.mentoringbackend.user.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.user.dto.UserProfileResponse;
import com.mentoring.mentoringbackend.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        return ApiResponse.success(userProfileService.getMyProfile());
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return ApiResponse.success(userProfileService.getUserProfile(userId));
    }
}
