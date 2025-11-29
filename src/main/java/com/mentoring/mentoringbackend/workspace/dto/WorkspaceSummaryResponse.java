package com.mentoring.mentoringbackend.workspace.dto;

import com.mentoring.mentoringbackend.workspace.domain.WorkspaceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkspaceSummaryResponse {

    private Long id;
    private String title;

    private Long programId;
    private String programName;

    private WorkspaceStatus status;

    private int mentorCount;
    private int menteeCount;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime createdAt;
}
