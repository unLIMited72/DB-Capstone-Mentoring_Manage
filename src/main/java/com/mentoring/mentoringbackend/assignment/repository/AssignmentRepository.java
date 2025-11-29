package com.mentoring.mentoringbackend.assignment.repository;

import com.mentoring.mentoringbackend.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // 워크스페이스 내 과제 목록 (마감일 기준 정렬)
    List<Assignment> findAllByWorkspaceIdOrderByDueDateAsc(Long workspaceId);
}
