package com.mentoring.mentoringbackend.assignment.repository;

import com.mentoring.mentoringbackend.assignment.domain.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findAllByAssignmentId(Long assignmentId);

    // ✅ 추가: 특정 과제 + 특정 유저의 제출 목록 (멘티 조회용)
    List<AssignmentSubmission> findAllByAssignmentIdAndUserId(Long assignmentId, Long userId);

    Optional<AssignmentSubmission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);
}
