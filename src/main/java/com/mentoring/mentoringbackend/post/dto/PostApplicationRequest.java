package com.mentoring.mentoringbackend.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostApplicationRequest {

    @NotNull
    private Long postId;

    // 선택 메시지
    private String message;
}
