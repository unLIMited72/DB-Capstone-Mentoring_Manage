package com.mentoring.mentoringbackend.post.dto;

import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.tag.dto.TagDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {

    private Long id;

    private Long programId;
    private String programName;

    private Long authorId;
    private String authorName;

    private PostType type;
    private PostStatus status;

    private String title;
    private String content;
    private String targetLevel;
    private Integer maxMembers;

    private Integer expectedWeeks;
    private Integer expectedSessionsTotal;
    private Integer expectedSessionsPerWeek;
    private String preferredMode;
    private String preferredTimeNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TagDto> tags;
}
