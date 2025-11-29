package com.mentoring.mentoringbackend.assignment.service;

import com.mentoring.mentoringbackend.assignment.domain.Assignment;
import com.mentoring.mentoringbackend.assignment.domain.AssignmentSubmission;
import com.mentoring.mentoringbackend.assignment.dto.AssignmentSubmissionRequest;
import com.mentoring.mentoringbackend.assignment.dto.AssignmentSubmissionResponse;
import com.mentoring.mentoringbackend.assignment.repository.AssignmentRepository;
import com.mentoring.mentoringbackend.assignment.repository.AssignmentSubmissionRepository;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentSubmissionService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public List<AssignmentSubmissionResponse> getSubmissions(Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        Long workspaceId = assignment.getWorkspace().getId();

        User me = userService.getCurrentUser();
        validateWorkspaceMember(workspaceId, me.getId());

        return assignmentSubmissionRepository.findAllByAssignmentId(assignmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssignmentSubmissionResponse submitOrUpdate(Long assignmentId,
                                                       AssignmentSubmissionRequest request) {
        Assignment assignment = getAssignment(assignmentId);
        Long workspaceId = assignment.getWorkspace().getId();

        User operator = userService.getCurrentUser();
        validateWorkspaceMember(workspaceId, operator.getId());
        validateWorkspaceMember(workspaceId, request.getUserId());

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByAssignmentIdAndUserId(assignmentId, request.getUserId())
                .orElse(null);

        if (submission == null) {
            submission = AssignmentSubmission.builder()
                    .assignment(assignment)
                    .user(targetUser)
                    .build();
        }

        // 멘티 제출
        if (request.getContent() != null) {
            submission.submit(request.getContent());
        }

        // 멘토 피드백/점수
        submission.updateFeedback(request.getFeedback(), request.getScore());

        // 아무 내용도 없으면 저장 의미 X
        if (submission.getContent() == null
                && submission.getFeedback() == null
                && submission.getScore() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "제출 내용 또는 피드백/점수 중 하나는 있어야 합니다.");
        }

        AssignmentSubmission saved = assignmentSubmissionRepository.save(submission);
        return toResponse(saved);
    }

    // === 내부 유틸 ===

    private Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "과제를 찾을 수 없습니다."));
    }

    private void validateWorkspaceMember(Long workspaceId, Long userId) {
        boolean isMember = workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));

        if (!isMember) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "해당 워크스페이스의 구성원이 아닙니다.");
        }
    }

    private AssignmentSubmissionResponse toResponse(AssignmentSubmission submission) {
        return AssignmentSubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getName())
                .content(submission.getContent())
                .submittedAt(submission.getSubmittedAt())
                .status(submission.getStatus())
                .feedback(submission.getFeedback())
                .score(submission.getScore())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
