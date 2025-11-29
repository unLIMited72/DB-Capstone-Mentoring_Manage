package com.mentoring.mentoringbackend.workspace.service;

import com.mentoring.mentoringbackend.academic.domain.Program;
import com.mentoring.mentoringbackend.academic.repository.ProgramRepository;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostApplication;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.domain.*;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceCreateRequest;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceDetailResponse;
import com.mentoring.mentoringbackend.workspace.dto.WorkspaceSummaryResponse;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceMemberRepository;
import com.mentoring.mentoringbackend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProgramRepository programRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 관리자/운영자 등이 수동으로 워크스페이스를 생성하는 경우
     */
    @Transactional
    public WorkspaceDetailResponse createWorkspace(WorkspaceCreateRequest request) {
        User creator = userService.getCurrentUser();

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "프로그램을 찾을 수 없습니다."));

        Post sourcePost = null;
        if (request.getSourcePostId() != null) {
            sourcePost = postRepository.findById(request.getSourcePostId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        }

        LocalDate startDate = Optional.ofNullable(request.getStartDate())
                .orElse(LocalDate.now());

        Workspace workspace = Workspace.builder()
                .program(program)
                .sourcePost(sourcePost)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(WorkspaceStatus.ACTIVE)
                .startDate(startDate)
                .endDate(request.getEndDate())
                .createdBy(creator)
                .build();

        Workspace saved = workspaceRepository.save(workspace);

        // 멤버 등록
        List<WorkspaceMember> toSaveMembers = new ArrayList<>();

        if (request.getMentorIds() != null) {
            List<User> mentors = userRepository.findAllById(request.getMentorIds());
            for (User mentor : mentors) {
                toSaveMembers.add(buildMember(saved, mentor, WorkspaceRole.MENTOR));
            }
        }

        if (request.getMenteeIds() != null) {
            List<User> mentees = userRepository.findAllById(request.getMenteeIds());
            for (User mentee : mentees) {
                toSaveMembers.add(buildMember(saved, mentee, WorkspaceRole.MENTEE));
            }
        }

        // 비어 있으면, 최소 생성자는 멘토로 넣어줘도 됨 (선택 로직)
        if (toSaveMembers.isEmpty()) {
            toSaveMembers.add(buildMember(saved, creator, WorkspaceRole.MENTOR));
        }

        workspaceMemberRepository.saveAll(toSaveMembers);

        return toDetailResponse(saved);
    }

    /**
     * PostApplication ACCEPT 시 자동으로 워크스페이스 생성
     */
    @Transactional
    public Workspace createWorkspaceFromAcceptedApplication(PostApplication application) {
        Post post = application.getPost();
        Program program = post.getProgram();

        String title = "[멘토링] " + post.getTitle();
        String description = post.getContent();

        Workspace workspace = Workspace.builder()
                .program(program)
                .sourcePost(post)
                .title(title)
                .description(description)
                .status(WorkspaceStatus.ACTIVE)
                .startDate(LocalDate.now())
                .createdBy(post.getAuthor())
                .build();

        Workspace saved = workspaceRepository.save(workspace);

        // 멘토/멘티 역할 결정
        User mentor;
        User mentee;

        if (post.getType() == PostType.MENTOR_RECRUIT) {
            mentor = post.getAuthor();
            mentee = application.getFromUser();
        } else {
            // MENTEE_REQUEST 인 경우: 글 작성자가 멘티, 신청자가 멘토
            mentor = application.getFromUser();
            mentee = post.getAuthor();
        }

        List<WorkspaceMember> members = List.of(
                buildMember(saved, mentor, WorkspaceRole.MENTOR),
                buildMember(saved, mentee, WorkspaceRole.MENTEE)
        );

        workspaceMemberRepository.saveAll(members);

        return saved;
    }

    public WorkspaceDetailResponse getWorkspace(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        return toDetailResponse(workspace);
    }

    /**
     * 내가 속한 워크스페이스 목록
     */
    public List<WorkspaceSummaryResponse> getMyWorkspaces() {
        User me = userService.getCurrentUser();
        List<WorkspaceMember> memberships = workspaceMemberRepository.findAllByUserId(me.getId());

        // workspace 중복 제거
        List<Workspace> workspaces = memberships.stream()
                .map(WorkspaceMember::getWorkspace)
                .distinct()
                .collect(Collectors.toList());

        return workspaces.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // ===== 내부 도우미 =====

    private WorkspaceMember buildMember(Workspace workspace, User user, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private WorkspaceSummaryResponse toSummaryResponse(Workspace workspace) {
        int mentorCount = (int) workspace.getMembers().stream()
                .filter(m -> m.getRole() == WorkspaceRole.MENTOR)
                .count();
        int menteeCount = (int) workspace.getMembers().stream()
                .filter(m -> m.getRole() == WorkspaceRole.MENTEE)
                .count();

        return WorkspaceSummaryResponse.builder()
                .id(workspace.getId())
                .title(workspace.getTitle())
                .programId(workspace.getProgram().getId())
                .programName(workspace.getProgram().getName())
                .status(workspace.getStatus())
                .mentorCount(mentorCount)
                .menteeCount(menteeCount)
                .startDate(workspace.getStartDate())
                .endDate(workspace.getEndDate())
                .createdAt(workspace.getCreatedAt())
                .build();
    }

    private WorkspaceDetailResponse toDetailResponse(Workspace workspace) {
        List<WorkspaceDetailResponse.Member> members = workspace.getMembers().stream()
                .map(m -> WorkspaceDetailResponse.Member.builder()
                        .userId(m.getUser().getId())
                        .name(m.getUser().getName())
                        .role(m.getRole())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList();

        return WorkspaceDetailResponse.builder()
                .id(workspace.getId())
                .title(workspace.getTitle())
                .description(workspace.getDescription())
                .programId(workspace.getProgram().getId())
                .programName(workspace.getProgram().getName())
                .status(workspace.getStatus())
                .startDate(workspace.getStartDate())
                .endDate(workspace.getEndDate())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .members(members)
                .build();
    }
}
