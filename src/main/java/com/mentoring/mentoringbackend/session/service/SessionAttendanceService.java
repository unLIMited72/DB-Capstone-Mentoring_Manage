package com.mentoring.mentoringbackend.session.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.session.domain.SessionAttendance;
import com.mentoring.mentoringbackend.session.domain.SessionStatus;
import com.mentoring.mentoringbackend.session.dto.SessionAttendanceRequest;
import com.mentoring.mentoringbackend.session.repository.SessionAttendanceRepository;
import com.mentoring.mentoringbackend.session.repository.SessionRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionAttendanceService {

    private final SessionRepository sessionRepository;
    private final SessionAttendanceRepository sessionAttendanceRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public List<SessionAttendanceRequest> getAttendance(Long sessionId) {
        Session session = getSession(sessionId);
        User me = userService.getCurrentUser();
        Long workspaceId = session.getWorkspace().getId();

        // ✅ 조회는 워크스페이스 멤버면 모두 가능
        validateWorkspaceMember(workspaceId, me.getId());

        return sessionAttendanceRepository.findAllBySession_Id(sessionId)
                .stream()
                .map(att -> SessionAttendanceRequest.builder()
                        .userId(att.getUser().getId())
                        .userName(att.getUser().getName())
                        .attendanceStatus(att.getAttendanceStatus())
                        .checkedAt(att.getCheckedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void markAttendance(Long sessionId, SessionAttendanceRequest request) {
        Session session = getSession(sessionId);
        Long workspaceId = session.getWorkspace().getId();

        User operator = userService.getCurrentUser();

        // ✅ 출석 체크는 멘토만
        validateWorkspaceMentor(workspaceId, operator.getId());

        // ✅ 취소된 세션에는 출석 체크 불가
        if (session.getStatus() == SessionStatus.CANCELED) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "취소된 세션에는 출석을 기록할 수 없습니다."
            );
        }

        // 출석 대상이 워크스페이스 멤버인지 확인
        validateWorkspaceMember(workspaceId, request.getUserId());

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        SessionAttendance attendance = sessionAttendanceRepository
                .findBySession_IdAndUser_Id(sessionId, request.getUserId())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        if (attendance == null) {
            attendance = SessionAttendance.builder()
                    .session(session)
                    .user(targetUser)
                    .attendanceStatus(request.getAttendanceStatus())
                    .checkedAt(now)
                    .build();
        } else {
            attendance.updateStatus(request.getAttendanceStatus(), now);
        }

        sessionAttendanceRepository.save(attendance);
    }

    // ==== 내부 도우미 ====

    private Session getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "세션을 찾을 수 없습니다."));
    }

    private void validateWorkspaceMember(Long workspaceId, Long userId) {
        boolean isMember = workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (!isMember) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "해당 워크스페이스의 구성원이 아닙니다.");
        }
    }

    // ✅ 새로 추가: 멘토 여부 확인
    private void validateWorkspaceMentor(Long workspaceId, Long userId) {
        boolean isMentor = workspaceMemberRepository
                .existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.MENTOR);

        if (!isMentor) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "멘토만 출석을 기록할 수 있습니다.");
        }
    }
}
