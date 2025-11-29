package com.mentoring.mentoringbackend.user.service;

import com.mentoring.mentoringbackend.academic.domain.Major;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserService userService;

    public UserProfileResponse getMyProfile() {
        User me = userService.getCurrentUser();
        return toResponse(me);
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userService.getById(userId);
        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        Major major = user.getMajor();
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .majorId(major != null ? major.getId() : null)
                .majorName(major != null ? major.getName() : null)
                .role(user.getRole())
                .active(user.getIsActive())
                .build();
    }
}
