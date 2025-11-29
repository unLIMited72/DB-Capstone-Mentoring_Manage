package com.mentoring.mentoringbackend.academic.web;

import com.mentoring.mentoringbackend.academic.dto.MajorDto;
import com.mentoring.mentoringbackend.academic.service.AcademicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academic/majors")
public class MajorController {

    private final AcademicService academicService;

    @GetMapping
    public List<MajorDto> getMajors() {
        return academicService.getAllMajors();
    }

    @PostMapping
    public ResponseEntity<MajorDto> createMajor(@RequestBody @Valid MajorDto request) {
        MajorDto created = academicService.createMajor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
