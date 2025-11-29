package com.mentoring.mentoringbackend.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WorkspaceCreateRequest {

    @NotNull
    private Long programId;

    // 선택: 어떤 모집글에서 파생된 워크스페이스인지
    private Long sourcePostId;

    @NotBlank
    private String title;

    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    // 초기 멤버 지정 (선택)
    private List<Long> mentorIds;
    private List<Long> menteeIds;
}
