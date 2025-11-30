package com.mentoring.mentoringbackend.workspace.repository;

import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findAllByProgramId(Long programId);

    List<Workspace> findAllByProgramIdAndStatus(Long programId, WorkspaceStatus status);
    

    Optional<Workspace> findByProgramIdAndSourcePostIdAndStatus(
            Long programId,
            Long sourcePostId,
            WorkspaceStatus status
    );
}
