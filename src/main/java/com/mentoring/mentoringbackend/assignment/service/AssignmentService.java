package com.mentoring.mentoringbackend.assignment.service;

import com.mentoring.mentoringbackend.assignment.domain.Assignment;
import com.mentoring.mentoringbackend.assignment.dto.AssignmentCreateRequest;
import com.mentoring.mentoringbackend.assignment.dto.AssignmentResponse;
import com.mentoring.mentoringbackend.assignment.repository.AssignmentRepository;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.session.repository.SessionRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceStatus;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceMemberRepository;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SessionRepository sessionRepository;
    private final UserService userService;

    @Transactional
    public AssignmentResponse createAssignment(Long workspaceId, AssignmentCreateRequest request) {
        User me = userService.getCurrentUser();
        // ✅ 과제 생성은 멘토만
        validateWorkspaceMentor(workspaceId, me.getId());

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        // ✅ FINISHED 워크스페이스에서는 과제 생성 불가
        if (workspace.getStatus() == WorkspaceStatus.FINISHED) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "종료된 워크스페이스에서는 새로운 과제를 생성할 수 없습니다."
            );
        }

        Session session = null;
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "세션을 찾을 수 없습니다."));

            if (!session.getWorkspace().getId().equals(workspaceId)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "이 워크스페이스에 속한 세션이 아닙니다.");
            }
        }

        Assignment assignment = Assignment.builder()
                .workspace(workspace)
                .session(session)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .createdBy(me)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved);
    }

    public List<AssignmentResponse> getAssignments(Long workspaceId) {
        User me = userService.getCurrentUser();
        // ✅ 조회는 멤버면 모두 가능
        validateWorkspaceMember(workspaceId, me.getId());

        return assignmentRepository.findAllByWorkspaceIdOrderByDueDateAsc(workspaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AssignmentResponse getAssignment(Long workspaceId, Long assignmentId) {
        User me = userService.getCurrentUser();
        validateWorkspaceMember(workspaceId, me.getId());

        Assignment assignment = findAssignmentBelongingToWorkspace(workspaceId, assignmentId);
        return toResponse(assignment);
    }

    @Transactional
    public AssignmentResponse updateAssignment(Long workspaceId,
                                               Long assignmentId,
                                               AssignmentCreateRequest request) {
        User me = userService.getCurrentUser();
        // ✅ 수정도 멘토만
        validateWorkspaceMentor(workspaceId, me.getId());

        Assignment assignment = findAssignmentBelongingToWorkspace(workspaceId, assignmentId);

        Workspace workspace = assignment.getWorkspace();
        if (workspace.getStatus() == WorkspaceStatus.FINISHED) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "종료된 워크스페이스의 과제는 수정할 수 없습니다."
            );
        }

        Session session = assignment.getSession();
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "세션을 찾을 수 없습니다."));
            if (!session.getWorkspace().getId().equals(workspaceId)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "이 워크스페이스에 속한 세션이 아닙니다.");
            }
        }

        assignment.update(
                request.getTitle(),
                request.getDescription(),
                request.getDueDate(),
                session
        );

        return toResponse(assignment);
    }

    // === 내부 유틸 ===

    private void validateWorkspaceMember(Long workspaceId, Long userId) {
        boolean isMember = workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (!isMember) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "해당 워크스페이스의 구성원이 아닙니다.");
        }
    }

    // ✅ 새로 추가: 멘토 권한 확인
    private void validateWorkspaceMentor(Long workspaceId, Long userId) {
        boolean isMentor = workspaceMemberRepository
                .existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.MENTOR);

        if (!isMentor) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "멘토만 이 작업을 수행할 수 있습니다.");
        }
    }

    private Assignment findAssignmentBelongingToWorkspace(Long workspaceId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "과제를 찾을 수 없습니다."));

        if (!assignment.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이 워크스페이스에 속한 과제가 아닙니다.");
        }
        return assignment;
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .workspaceId(assignment.getWorkspace().getId())
                .sessionId(assignment.getSession() != null ? assignment.getSession().getId() : null)
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .createdById(assignment.getCreatedBy().getId())
                .createdByName(assignment.getCreatedBy().getName())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
