package com.mentoring.mentoringbackend.academic.service;

import com.mentoring.mentoringbackend.academic.domain.*;
import com.mentoring.mentoringbackend.academic.dto.MajorDto;
import com.mentoring.mentoringbackend.academic.dto.ProgramDto;
import com.mentoring.mentoringbackend.academic.dto.SemesterDto;
import com.mentoring.mentoringbackend.academic.repository.MajorRepository;
import com.mentoring.mentoringbackend.academic.repository.ProgramRepository;
import com.mentoring.mentoringbackend.academic.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mentoring.mentoringbackend.academic.domain.AcademicMapper.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicService {

    private final MajorRepository majorRepository;
    private final SemesterRepository semesterRepository;
    private final ProgramRepository programRepository;

    // ----- Major -----
    public List<MajorDto> getAllMajors() {
        return majorRepository.findAll()
                .stream()
                .map(AcademicMapper::toDto)
                .toList();
    }

    @Transactional
    public MajorDto createMajor(MajorDto request) {
        Major major = Major.builder()
                .name(request.getName())
                .build();
        Major saved = majorRepository.save(major);
        return toDto(saved);
    }

    // ----- Semester -----
    public List<SemesterDto> getAllSemesters() {
        return semesterRepository.findAll()
                .stream()
                .map(AcademicMapper::toDto)
                .toList();
    }

    public List<SemesterDto> getActiveSemesters() {
        return semesterRepository.findByIsActiveTrue()
                .stream()
                .map(AcademicMapper::toDto)
                .toList();
    }

    @Transactional
    public SemesterDto createSemester(SemesterDto request) {
        Semester semester = Semester.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .build();
        Semester saved = semesterRepository.save(semester);
        return toDto(saved);
    }

    public Semester getSemesterEntity(Long semesterId) {
        return semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found: " + semesterId));
    }

    // ----- Program -----
    public List<ProgramDto> getPrograms(Long semesterId, boolean onlyActive) {
        if (semesterId != null) {
            return programRepository.findBySemesterId(semesterId)
                    .stream()
                    .filter(p -> !onlyActive || Boolean.TRUE.equals(p.getIsActive()))
                    .map(AcademicMapper::toDto)
                    .toList();
        }

        List<Program> programs = onlyActive
                ? programRepository.findByIsActiveTrue()
                : programRepository.findAll();

        return programs.stream()
                .map(AcademicMapper::toDto)
                .toList();
    }

    @Transactional
    public ProgramDto createProgram(ProgramDto request) {
        Semester semester = getSemesterEntity(request.getSemesterId());

        Program program = Program.builder()
                .semester(semester)
                .name(request.getName())
                .type(request.getType())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .build();

        Program saved = programRepository.save(program);
        return toDto(saved);
    }
}
