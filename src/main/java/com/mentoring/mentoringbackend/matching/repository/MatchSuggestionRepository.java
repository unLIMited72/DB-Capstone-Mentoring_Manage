package com.mentoring.mentoringbackend.matching.repository;

import com.mentoring.mentoringbackend.matching.domain.MatchSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchSuggestionRepository extends JpaRepository<MatchSuggestion, Long> {

    // 🔹 프로그램 + 멘티 기준으로 기존 추천 기록 전체 삭제
    void deleteByProgramIdAndMenteeId(Long programId, Long menteeId);

    // (선택) 나중에 로그 조회용으로 쓸 수 있음
    List<MatchSuggestion> findAllByProgramIdAndMenteeIdOrderByScoreDesc(Long programId, Long menteeId);
}
