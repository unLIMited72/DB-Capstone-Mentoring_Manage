package com.mentoring.mentoringbackend.assignment.dto;

import com.mentoring.mentoringbackend.assignment.domain.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentSubmissionResponse {

    private Long id;
    private Long assignmentId;

    private Long userId;
    private String userName;

    private String content;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;

    private String feedback;
    private Integer score;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
