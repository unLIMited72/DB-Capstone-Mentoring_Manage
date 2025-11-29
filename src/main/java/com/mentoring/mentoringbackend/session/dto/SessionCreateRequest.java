package com.mentoring.mentoringbackend.session.dto;

import com.mentoring.mentoringbackend.session.domain.SessionMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionCreateRequest {

    // workspaceId는 URL Path로 받으므로 여기서는 제외

    private Integer weekIndex;

    @NotBlank
    private String topic;

    @NotNull
    private LocalDateTime scheduledAt;

    @NotNull
    private SessionMode mode;

    // 선택 입력
    private String plan;
    private String note;
    private String homeworkSummary;
}
