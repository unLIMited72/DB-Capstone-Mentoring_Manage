package com.mentoring.mentoringbackend.post.dto;

import com.mentoring.mentoringbackend.post.domain.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostApplicationResponse {

    private Long id;
    private Long postId;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private ApplicationStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}
