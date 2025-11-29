package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSignupRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 20)
    private String studentId;

    @NotNull
    private Long majorId;

    @NotNull
    private Role role;
}
