package com.mentoring.mentoringbackend.workspace.repository;

import com.mentoring.mentoringbackend.workspace.domain.WorkspaceMember;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    List<WorkspaceMember> findAllByUserId(Long userId);

    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);

    long countByWorkspaceIdAndRole(Long workspaceId, WorkspaceRole role);
}
