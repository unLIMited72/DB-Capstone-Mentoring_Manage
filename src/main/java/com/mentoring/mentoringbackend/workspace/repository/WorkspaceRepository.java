package com.mentoring.mentoringbackend.workspace.repository;

import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findAllByProgramId(Long programId);

    List<Workspace> findAllByProgramIdAndStatus(Long programId, WorkspaceStatus status);
}
