package com.mentoring.mentoringbackend.matching.repository;

import com.mentoring.mentoringbackend.matching.domain.MatchingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchingConfigRepository extends JpaRepository<MatchingConfig, Long> {

    Optional<MatchingConfig> findByProgramId(Long programId);
}
