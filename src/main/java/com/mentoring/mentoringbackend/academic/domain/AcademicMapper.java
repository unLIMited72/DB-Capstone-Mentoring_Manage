package com.mentoring.mentoringbackend.academic.domain;

import com.mentoring.mentoringbackend.academic.dto.MajorDto;
import com.mentoring.mentoringbackend.academic.dto.ProgramDto;
import com.mentoring.mentoringbackend.academic.dto.SemesterDto;

public final class AcademicMapper {

    private AcademicMapper() {
    }

    public static MajorDto toDto(Major major) {
        if (major == null) return null;
        return MajorDto.builder()
                .id(major.getId())
                .name(major.getName())
                .build();
    }

    public static SemesterDto toDto(Semester semester) {
        if (semester == null) return null;
        return SemesterDto.builder()
                .id(semester.getId())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .isActive(semester.getIsActive())
                .build();
    }

    public static ProgramDto toDto(Program program) {
        if (program == null) return null;
        Long semesterId = program.getSemester() != null ? program.getSemester().getId() : null;

        return ProgramDto.builder()
                .id(program.getId())
                .semesterId(semesterId)
                .name(program.getName())
                .type(program.getType())
                .isActive(program.getIsActive())
                .build();
    }
}
