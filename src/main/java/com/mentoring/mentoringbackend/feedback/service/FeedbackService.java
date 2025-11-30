package com.mentoring.mentoringbackend.feedback.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.feedback.domain.Feedback;
import com.mentoring.mentoringbackend.feedback.domain.FeedbackTargetType;
import com.mentoring.mentoringbackend.feedback.dto.FeedbackRequest;
import com.mentoring.mentoringbackend.feedback.dto.FeedbackResponse;
import com.mentoring.mentoringbackend.feedback.repository.FeedbackRepository;
import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.session.repository.SessionRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceMemberRepository;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final UserRepository userRepository; // ✅ 추가

    @Transactional
    public FeedbackResponse createFeedback(Long workspaceId, FeedbackRequest request) {
        User me = userService.getCurrentUser();
        // ✅ fromUser(나)가 이 워크스페이스 멤버인지
        validateWorkspaceMember(workspaceId, me.getId());

        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        Session session = null;
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "세션을 찾을 수 없습니다."));
            if (!session.getWorkspace().getId().equals(workspaceId)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "이 워크스페이스에 속한 세션이 아닙니다.");
            }
        }

        int rating = request.getRating();
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "평점은 1~5 사이여야 합니다.");
        }

        FeedbackTargetType targetType = request.getTargetType();
        User toUser = null;

        // ✅ targetType에 따라 대상 유저 처리
        if (targetType == FeedbackTargetType.PROGRAM) {
            // 프로그램 전체 평가 → 대상 유저 지정하면 안 됨
            if (request.getToUserId() != null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "프로그램 전체 평가는 대상 사용자를 지정할 수 없습니다.");
            }
        } else {
            // MENTOR or MENTEE → 대상 유저 필수
            if (request.getToUserId() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "멘토/멘티 평가에는 대상 사용자가 필요합니다.");
            }

            toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "대상 사용자를 찾을 수 없습니다."));

            // ✅ 대상 유저도 같은 워크스페이스 멤버인지 확인 (질문했던 부분)
            validateWorkspaceMember(workspaceId, toUser.getId());

            // (선택) targetType == MENTOR면 멘토 역할인지, MENTEE면 멘티 역할인지 까지 체크하고 싶으면:
            validateTargetRole(workspaceId, toUser.getId(), targetType);
        }

        boolean anonymous = request.getAnonymous() != null && request.getAnonymous();

        Feedback feedback = Feedback.builder()
            .workspace(workspace)
            .session(session)
            .fromUser(me)
            .toUser(toUser)
            .targetType(targetType)
            .rating(rating)
            .comment(request.getComment())
            .anonymous(anonymous)
            .build();

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }   


    public List<FeedbackResponse> getFeedbacks(Long workspaceId, Long sessionId) {
        User me = userService.getCurrentUser();
        validateWorkspaceMember(workspaceId, me.getId());

        List<Feedback> list;
        if (sessionId != null) {
            list = feedbackRepository.findAllByWorkspaceIdAndSessionIdOrderByCreatedAtDesc(workspaceId, sessionId);
        } else {
            list = feedbackRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
        }

        return list.stream()
                .map(this::toResponse)
                .toList();
    }

    // === 내부 유틸 ===

    private void validateWorkspaceMember(Long workspaceId, Long userId) {
        boolean isMember = workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (!isMember) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "해당 워크스페이스의 구성원이 아닙니다.");
        }
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        boolean anonymous = feedback.isAnonymous();

        Long fromUserId = anonymous ? null : feedback.getFromUser().getId();
        String fromUserName = anonymous ? "익명" : feedback.getFromUser().getName();

        Long toUserId = feedback.getToUser() != null ? feedback.getToUser().getId() : null;
        String toUserName = feedback.getToUser() != null ? feedback.getToUser().getName() : null;

        return FeedbackResponse.builder()
            .id(feedback.getId())
            .workspaceId(feedback.getWorkspace().getId())
            .sessionId(feedback.getSession() != null ? feedback.getSession().getId() : null)
            .targetType(feedback.getTargetType())
            .fromUserId(fromUserId)
            .fromUserName(fromUserName)
            .toUserId(toUserId)
            .toUserName(toUserName)
            .rating(feedback.getRating())
            .comment(feedback.getComment())
            .anonymous(anonymous)
            .createdAt(feedback.getCreatedAt())
            .updatedAt(feedback.getUpdatedAt())
            .build();
    }

    private void validateTargetRole(Long workspaceId, Long userId, FeedbackTargetType targetType) {
        if (targetType == FeedbackTargetType.MENTOR) {
            boolean isMentor = workspaceMemberRepository
                .existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.MENTOR);
            if (!isMentor) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "멘토 평가 대상은 멘토 역할이어야 합니다.");
            }
        } else if (targetType == FeedbackTargetType.MENTEE) {
            boolean isMentee = workspaceMemberRepository
                .existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.MENTEE);
            if (!isMentee) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "멘티 평가 대상은 멘티 역할이어야 합니다.");
            }
        }
    }
}
