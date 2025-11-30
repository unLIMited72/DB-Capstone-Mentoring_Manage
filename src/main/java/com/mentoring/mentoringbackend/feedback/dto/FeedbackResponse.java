package com.mentoring.mentoringbackend.feedback.dto;

import com.mentoring.mentoringbackend.feedback.domain.FeedbackTargetType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {

    private Long id;

    private Long workspaceId;
    private Long sessionId;

    // ✅ 추가
    private FeedbackTargetType targetType;

    private Long fromUserId;        // anonymous=true 일 때는 null
    private String fromUserName;    // anonymous=true 이면 "익명"

    // ✅ 추가: 평가 대상
    private Long toUserId;
    private String toUserName;

    private Integer rating;
    private String comment;

    private boolean anonymous;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
