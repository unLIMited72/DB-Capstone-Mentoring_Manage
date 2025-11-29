package com.mentoring.mentoringbackend.feedback.repository;

import com.mentoring.mentoringbackend.feedback.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // 워크스페이스 전체 피드백
    List<Feedback> findAllByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    // 특정 세션에 대한 피드백
    List<Feedback> findAllByWorkspaceIdAndSessionIdOrderByCreatedAtDesc(Long workspaceId, Long sessionId);
}
