package com.mentoring.mentoringbackend.post.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.post.domain.ApplicationStatus;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostApplication;
import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.dto.PostApplicationRequest;
import com.mentoring.mentoringbackend.post.dto.PostApplicationResponse;
import com.mentoring.mentoringbackend.post.repository.PostApplicationRepository;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostApplicationService {

    private final PostApplicationRepository postApplicationRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final WorkspaceService workspaceService;

@Transactional
public PostApplicationResponse apply(PostApplicationRequest request) {
    User me = userService.getCurrentUser();
    Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시글을 찾을 수 없습니다."));

    // 1) 자기 글 신청 방지
    if (post.getAuthor().getId().equals(me.getId())) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "자기 자신의 글에는 신청할 수 없습니다.");
    }

    // 2) 모집 상태 체크 (OPEN 에만 신청 가능)
    if (post.getStatus() != PostStatus.OPEN) {
        throw new BusinessException(ErrorCode.INVALID_REQUEST, "모집이 종료된 게시글입니다.");
    }

    // 3) 같은 쌍에 이미 PENDING 신청 있는지 체크
    boolean existsPending = postApplicationRepository
            .existsByPostIdAndFromUserIdAndToUserIdAndStatus(
                    post.getId(),
                    me.getId(),
                    post.getAuthor().getId(),
                    ApplicationStatus.PENDING
            );
    if (existsPending) {
        throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 대기 중인 신청이 있습니다.");
    }

    PostApplication application = PostApplication.builder()
            .post(post)
            .fromUser(me)               // 신청자 (멘티든 멘토든 상관 없음)
            .toUser(post.getAuthor())   // 항상 글 작성자에게 신청이 감
            .status(ApplicationStatus.PENDING)
            .message(request.getMessage())
            .build();

    PostApplication saved = postApplicationRepository.save(application);
    return toResponse(saved);
}


    public List<PostApplicationResponse> getMySentApplications() {
        User me = userService.getCurrentUser();
        return postApplicationRepository.findAllByFromUserIdOrderByCreatedAtDesc(me.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PostApplicationResponse> getApplicationsToMe() {
        User me = userService.getCurrentUser();
        return postApplicationRepository.findAllByToUserIdOrderByCreatedAtDesc(me.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

@Transactional
public PostApplicationResponse accept(Long applicationId) {
    User me = userService.getCurrentUser();
    PostApplication application = getApplicationForDecision(me, applicationId);

    Post post = application.getPost();

    // 글이 이미 마감된 경우 수락 불가
    if (post.getStatus() == PostStatus.CLOSED) {
        throw new BusinessException(ErrorCode.INVALID_REQUEST, "마감된 모집글의 신청은 수락할 수 없습니다.");
    }

    application.decide(ApplicationStatus.ACCEPTED, LocalDateTime.now());

    // 첫 수락 시 OPEN -> MATCHED 로 변경
    if (post.getStatus() == PostStatus.OPEN) {
        post.changeStatus(PostStatus.MATCHED);
    }

    // 워크스페이스 자동 생성 (멘토/멘티 역할은 WorkspaceService 쪽에서 처리)
    workspaceService.createWorkspaceFromAcceptedApplication(application);

    return toResponse(application);
}


    @Transactional
    public PostApplicationResponse reject(Long applicationId) {
        User me = userService.getCurrentUser();
        PostApplication application = getApplicationForDecision(me, applicationId);

        application.decide(ApplicationStatus.REJECTED, LocalDateTime.now());
        return toResponse(application);
    }

    // ==== 내부 도우미 ====

    private PostApplication getApplicationForDecision(User me, Long applicationId) {
        PostApplication application = postApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "신청을 찾을 수 없습니다."));

        if (!application.getToUser().getId().equals(me.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이 신청을 처리할 권한이 없습니다.");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이미 처리된 신청입니다.");
        }

        return application;
    }

    private PostApplicationResponse toResponse(PostApplication application) {
        return PostApplicationResponse.builder()
                .id(application.getId())
                .postId(application.getPost().getId())
                .fromUserId(application.getFromUser().getId())
                .fromUserName(application.getFromUser().getName())
                .toUserId(application.getToUser().getId())
                .toUserName(application.getToUser().getName())
                .status(application.getStatus())
                .message(application.getMessage())
                .createdAt(application.getCreatedAt())
                .decidedAt(application.getDecidedAt())
                .build();
    }
}
