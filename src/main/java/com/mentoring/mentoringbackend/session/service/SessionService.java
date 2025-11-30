package com.mentoring.mentoringbackend.session.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.session.domain.SessionStatus;
import com.mentoring.mentoringbackend.session.dto.SessionCreateRequest;
import com.mentoring.mentoringbackend.session.dto.SessionResponse;
import com.mentoring.mentoringbackend.session.dto.SessionUpdateRequest;
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
public class SessionService {

    private final SessionRepository sessionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;

    @Transactional
    public SessionResponse createSession(Long workspaceId, SessionCreateRequest request) {
        User me = userService.getCurrentUser();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        // ✅ 워크스페이스 멤버 + 멘토 여부 검사
        validateWorkspaceMentor(workspaceId, me.getId());

        // ✅ FINISHED 워크스페이스에서는 세션 생성 불가
        if (workspace.getStatus() == WorkspaceStatus.FINISHED) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "종료된 워크스페이스에서는 새로운 세션을 생성할 수 없습니다."
            );
        }

        Session session = Session.builder()
                .workspace(workspace)
                .weekIndex(request.getWeekIndex())
                .topic(request.getTopic())
                .scheduledAt(request.getScheduledAt())
                .mode(request.getMode())
                .status(SessionStatus.PLANNED)
                .plan(request.getPlan())
                .note(request.getNote())
                .homeworkSummary(request.getHomeworkSummary())
                .build();

        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    public List<SessionResponse> getSessionsByWorkspace(Long workspaceId) {
        User me = userService.getCurrentUser();
        // ✅ 조회는 멤버이면 모두 가능 (멘토/멘티)
        validateWorkspaceMember(workspaceId, me.getId());

        return sessionRepository.findAllByWorkspaceIdOrderByScheduledAtAsc(workspaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SessionResponse getSession(Long workspaceId, Long sessionId) {
        User me = userService.getCurrentUser();
        validateWorkspaceMember(workspaceId, me.getId());

        Session session = findSessionBelongingToWorkspace(workspaceId, sessionId);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse updateSession(Long workspaceId, Long sessionId, SessionUpdateRequest request) {
        User me = userService.getCurrentUser();
        // ✅ 세션 수정도 멘토만 가능
        validateWorkspaceMentor(workspaceId, me.getId());

        Session session = findSessionBelongingToWorkspace(workspaceId, sessionId);

        // (선택) FINISHED 워크스페이스에서 수정 제한을 하고 싶으면 여기 체크 추가 가능

        session.updateBasicInfo(
                request.getTopic(),
                request.getScheduledAt(),
                request.getMode(),
                request.getWeekIndex()
        );
        session.updateDetail(
                request.getPlan(),
                request.getNote(),
                request.getHomeworkSummary()
        );
        session.changeStatus(request.getStatus());

        return toResponse(session);
    }

    // ==== 내부 도우미 ====

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

    private Session findSessionBelongingToWorkspace(Long workspaceId, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "세션을 찾을 수 없습니다."));

        if (!session.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이 워크스페이스에 속한 세션이 아닙니다.");
        }
        return session;
    }

    private SessionResponse toResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .workspaceId(session.getWorkspace().getId())
                .weekIndex(session.getWeekIndex())
                .topic(session.getTopic())
                .scheduledAt(session.getScheduledAt())
                .mode(session.getMode())
                .status(session.getStatus())
                .plan(session.getPlan())
                .note(session.getNote())
                .homeworkSummary(session.getHomeworkSummary())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
