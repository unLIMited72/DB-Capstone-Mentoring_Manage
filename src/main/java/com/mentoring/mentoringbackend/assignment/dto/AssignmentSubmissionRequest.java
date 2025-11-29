package com.mentoring.mentoringbackend.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentSubmissionRequest {

    @NotNull
    private Long userId;   // 제출/채점 대상 사용자

    // 멘티 제출용
    private String content;

    // 멘토 피드백/점수용 (선택)
    private String feedback;
    private Integer score;
}
