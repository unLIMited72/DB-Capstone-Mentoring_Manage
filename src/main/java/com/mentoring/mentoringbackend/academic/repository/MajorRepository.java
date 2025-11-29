package com.mentoring.mentoringbackend.academic.repository;

import com.mentoring.mentoringbackend.academic.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {

    Optional<Major> findByName(String name);
}
