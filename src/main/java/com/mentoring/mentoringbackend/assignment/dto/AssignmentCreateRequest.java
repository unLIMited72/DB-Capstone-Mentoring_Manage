package com.mentoring.mentoringbackend.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentCreateRequest {

    // workspaceId 는 URL path 로 받음
    private Long sessionId;    // 특정 세션에 묶고 싶을 때 (선택)

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime dueDate;
}
