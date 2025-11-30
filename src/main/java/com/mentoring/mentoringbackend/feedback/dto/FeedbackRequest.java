package com.mentoring.mentoringbackend.feedback.dto;

import com.mentoring.mentoringbackend.feedback.domain.FeedbackTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    private Long sessionId;   // 특정 세션에 대한 피드백이면 설정, 아니면 null

    // ✅ 추가: 대상 유저 (프로그램 전체 평가면 null)
    private Long toUserId;

    // ✅ 추가: 어떤 타입의 피드백인지
    @NotNull
    private FeedbackTargetType targetType; // PROGRAM / MENTOR / MENTEE

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    // 익명 여부 (null 이면 false 취급)
    private Boolean anonymous;
}
