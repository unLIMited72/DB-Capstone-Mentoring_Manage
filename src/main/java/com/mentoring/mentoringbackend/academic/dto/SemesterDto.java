package com.mentoring.mentoringbackend.academic.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
