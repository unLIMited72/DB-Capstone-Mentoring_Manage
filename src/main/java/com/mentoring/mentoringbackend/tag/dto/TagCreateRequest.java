package com.mentoring.mentoringbackend.tag.dto;

import com.mentoring.mentoringbackend.tag.domain.TagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TagCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private TagType type;

    // 시스템 기본 태그인지?
    private boolean system = false;

    // 매칭 알고리즘에 사용할지?
    private boolean matchable = true;

    // 부모 태그 (선택)
    private Long parentTagId;

    private String description;
}
