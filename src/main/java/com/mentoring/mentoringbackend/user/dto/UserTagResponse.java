package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.tag.domain.TagType;
import com.mentoring.mentoringbackend.user.domain.RelationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTagResponse {

    private Long id;

    private Long tagId;
    private String tagName;
    private TagType tagType;

    private RelationType relationType;
    private Integer level;
}
