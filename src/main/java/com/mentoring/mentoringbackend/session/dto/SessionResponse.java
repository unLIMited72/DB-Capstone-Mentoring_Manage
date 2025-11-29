package com.mentoring.mentoringbackend.session.dto;

import com.mentoring.mentoringbackend.session.domain.SessionMode;
import com.mentoring.mentoringbackend.session.domain.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionResponse {

    private Long id;
    private Long workspaceId;

    private Integer weekIndex;
    private String topic;
    private LocalDateTime scheduledAt;

    private SessionMode mode;
    private SessionStatus status;

    private String plan;
    private String note;
    private String homeworkSummary;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
