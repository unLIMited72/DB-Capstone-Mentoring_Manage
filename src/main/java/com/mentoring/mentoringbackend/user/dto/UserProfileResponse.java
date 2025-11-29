package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.user.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String name;
    private String studentId;
    private Long majorId;
    private String majorName;
    private Role role;
    private Boolean active;
}
