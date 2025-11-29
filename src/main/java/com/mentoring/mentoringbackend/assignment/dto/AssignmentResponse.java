package com.mentoring.mentoringbackend.assignment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {

    private Long id;
    private Long workspaceId;
    private Long sessionId;

    private String title;
    private String description;
    private LocalDateTime dueDate;

    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
