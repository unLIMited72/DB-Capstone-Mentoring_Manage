package com.mentoring.mentoringbackend.academic.repository;

import com.mentoring.mentoringbackend.academic.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findByName(String name);

    List<Semester> findByIsActiveTrue();
}
