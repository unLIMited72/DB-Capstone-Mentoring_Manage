package com.mentoring.mentoringbackend.assignment.repository;

import com.mentoring.mentoringbackend.assignment.domain.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findAllByAssignmentId(Long assignmentId);

    Optional<AssignmentSubmission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);
}
