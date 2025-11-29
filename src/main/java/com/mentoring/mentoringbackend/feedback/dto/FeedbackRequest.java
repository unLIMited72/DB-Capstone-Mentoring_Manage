package com.mentoring.mentoringbackend.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    // workspaceId 는 URL path 에서 받음
    private Long sessionId;     // 특정 세션에 대한 피드백이면 설정, 아니면 null

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    // 익명 여부 (null 이면 false 취급)
    private Boolean anonymous;
}
