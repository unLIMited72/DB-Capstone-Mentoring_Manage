package com.mentoring.mentoringbackend.post.dto;

import com.mentoring.mentoringbackend.post.domain.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {

    @NotNull
    private Long programId;

    @NotNull
    private PostType type;   // MENTOR_RECRUIT / MENTEE_REQUEST

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String targetLevel;

    private Integer maxMembers;

    // 선택 확장
    private Integer expectedWeeks;
    private Integer expectedSessionsTotal;
    private Integer expectedSessionsPerWeek;
    private String preferredMode;
    private String preferredTimeNote;

    // 게시글에 붙일 태그들 (선택)
    private List<Long> tagIds;
}
