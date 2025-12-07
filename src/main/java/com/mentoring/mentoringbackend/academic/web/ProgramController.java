package com.mentoring.mentoringbackend.academic.web;

import com.mentoring.mentoringbackend.academic.domain.ProgramType;
import com.mentoring.mentoringbackend.academic.dto.ProgramDto;
import com.mentoring.mentoringbackend.academic.service.AcademicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/academic/programs")
public class ProgramController {

    private final AcademicService academicService;

    @GetMapping
    public List<ProgramDto> getPrograms(
            @RequestParam(name = "semesterId", required = false) Long semesterId,
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly
    ) {
        ProgramDto dummy = ProgramDto.builder()
                .id(1L)
                .semesterId(1L)
                .name("한서튜터링")
                .type(ProgramType.TUTORING)
                .isActive(true)
                .build();

        return List.of(dummy);
        // ���߿� DB ���� ��: return academicService.getPrograms(semesterId, activeOnly);
    }

    @PostMapping
    public ResponseEntity<ProgramDto> createProgram(@RequestBody @Valid ProgramDto request) {
        ProgramDto created = academicService.createProgram(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
