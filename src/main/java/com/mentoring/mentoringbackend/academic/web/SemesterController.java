package com.mentoring.mentoringbackend.academic.web;

import com.mentoring.mentoringbackend.academic.dto.SemesterDto;
import com.mentoring.mentoringbackend.academic.service.AcademicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academic/semesters")
public class SemesterController {

    private final AcademicService academicService;

    @GetMapping
    public List<SemesterDto> getSemesters(@RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return academicService.getActiveSemesters();
        }
        return academicService.getAllSemesters();
    }

    @PostMapping
    public ResponseEntity<SemesterDto> createSemester(@RequestBody @Valid SemesterDto request) {
        SemesterDto created = academicService.createSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
