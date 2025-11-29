package com.mentoring.mentoringbackend.feedback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {

    private Long id;

    private Long workspaceId;
    private Long sessionId;

    private Long fromUserId;        // anonymous=true 일 때는 null 로 내려줄 예정
    private String fromUserName;    // anonymous=true 이면 "익명"

    private Integer rating;
    private String comment;

    private boolean anonymous;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
