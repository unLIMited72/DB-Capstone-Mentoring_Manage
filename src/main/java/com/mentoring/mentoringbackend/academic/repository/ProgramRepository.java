package com.mentoring.mentoringbackend.academic.repository;

import com.mentoring.mentoringbackend.academic.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    List<Program> findBySemesterId(Long semesterId);

    List<Program> findByIsActiveTrue();
}
