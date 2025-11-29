package com.mentoring.mentoringbackend.academic.dto;

import com.mentoring.mentoringbackend.academic.domain.ProgramType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramDto {
    private Long id;
    private Long semesterId;
    private String name;
    private ProgramType type;
    private Boolean isActive;
}
