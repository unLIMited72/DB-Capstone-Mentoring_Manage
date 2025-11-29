package com.mentoring.mentoringbackend.matching.dto;

import com.mentoring.mentoringbackend.post.domain.PostType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchingSuggestionResponse {

    private Long postId;
    private String title;
    private PostType type;

    private Long mentorId;
    private String mentorName;

    private double score;
}
