package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.user.domain.RelationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserTagRequest {

    @NotNull
    private Long tagId;

    @NotNull
    private RelationType relationType;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer level;
}
