package com.mentoring.mentoringbackend.matching.repository;

import com.mentoring.mentoringbackend.matching.domain.MatchSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchSuggestionRepository extends JpaRepository<MatchSuggestion, Long> {
    // 필요해지면 프로그램/멘티 기준 조회 메서드 추가 가능
}
