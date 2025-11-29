package com.mentoring.mentoringbackend.feedback.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.feedback.domain.Feedback;
import com.mentoring.mentoringbackend.feedback.dto.FeedbackRequest;
import com.mentoring.mentoringbackend.feedback.dto.FeedbackResponse;
import com.mentoring.mentoringbackend.feedback.repository.FeedbackRepository;
import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.session.repository.SessionRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.domain.Workspace;
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

    @Transactional
    public FeedbackResponse createFeedback(Long workspaceId, FeedbackRequest request) {
        User me = userService.getCurrentUser();
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

        boolean anonymous = request.getAnonymous() != null && request.getAnonymous();

        Feedback feedback = Feedback.builder()
                .workspace(workspace)
                .session(session)
                .fromUser(me)
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

        return FeedbackResponse.builder()
                .id(feedback.getId())
                .workspaceId(feedback.getWorkspace().getId())
                .sessionId(feedback.getSession() != null ? feedback.getSession().getId() : null)
                .fromUserId(fromUserId)
                .fromUserName(fromUserName)
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .anonymous(anonymous)
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
