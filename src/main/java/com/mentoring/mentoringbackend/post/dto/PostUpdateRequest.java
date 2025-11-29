package com.mentoring.mentoringbackend.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class PostUpdateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String targetLevel;
    private Integer maxMembers;

    private Integer expectedWeeks;
    private Integer expectedSessionsTotal;
    private Integer expectedSessionsPerWeek;
    private String preferredMode;
    private String preferredTimeNote;

    private List<Long> tagIds;
}
