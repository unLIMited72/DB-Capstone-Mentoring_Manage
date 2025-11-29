package com.mentoring.mentoringbackend.tag.dto;

import com.mentoring.mentoringbackend.tag.domain.TagType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagDto {

    private Long id;
    private String name;
    private TagType type;
    private boolean system;
    private boolean matchable;
    private Long parentTagId;
    private String parentTagName;
    private String description;
}
