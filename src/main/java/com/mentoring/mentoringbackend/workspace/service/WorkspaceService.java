package com.mentoring.mentoringbackend.workspace.service;

import com.mentoring.mentoringbackend.academic.domain.Program;
import com.mentoring.mentoringbackend.academic.repository.ProgramRepository;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostApplication;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.user.domain.ContactInfo;
import com.mentoring.mentoringbackend.user.domain.ContactType;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.repository.ContactInfoRepository;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.service.UserService;
import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceMember;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceStatus;
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
    private final ContactInfoRepository contactInfoRepository;

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
     * PostApplication ACCEPT 시
     * - 기존 워크스페이스가 있으면 재사용
     * - 없으면 새로 생성
     * => 멘토 1 + 멘티 N 구조 지원
     */
    @Transactional
    public Workspace createWorkspaceFromAcceptedApplication(PostApplication application) {
        Post post = application.getPost();
        Program program = post.getProgram();

        // 1) 같은 프로그램 + 같은 sourcePost + ACTIVE 상태 워크스페이스가 있으면 재사용
        Workspace workspace = workspaceRepository
                .findByProgramIdAndSourcePostIdAndStatus(
                        program.getId(),
                        post.getId(),
                        WorkspaceStatus.ACTIVE
                )
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    String title = "[멘토링] " + post.getTitle();
                    String description = post.getContent();

                    Workspace newWs = Workspace.builder()
                            .program(program)
                            .sourcePost(post)
                            .title(title)
                            .description(description)
                            .status(WorkspaceStatus.ACTIVE)
                            .startDate(LocalDate.now())
                            .createdBy(post.getAuthor())
                            .build();

                    return workspaceRepository.save(newWs);
                });

        // 2) 멘토/멘티 역할 결정
        User mentor;
        User mentee;

        if (post.getType() == PostType.MENTOR_RECRUIT) {
            // 멘토 모집글: 글 작성자 = 멘토, 신청자 = 멘티
            mentor = post.getAuthor();
            mentee = application.getFromUser();
        } else {
            // 멘티 요청글: 글 작성자 = 멘티, 신청자 = 멘토
            mentor = application.getFromUser();
            mentee = post.getAuthor();
        }

        // 3) 멤버 중복 가입 방지 후 추가
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), mentor.getId())) {
            workspaceMemberRepository.save(buildMember(workspace, mentor, WorkspaceRole.MENTOR));
        }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), mentee.getId())) {
            workspaceMemberRepository.save(buildMember(workspace, mentee, WorkspaceRole.MENTEE));
        }

        // 4) 워크스페이스 전용 연락처 보장 (이 워크스페이스 안에서 서로 이메일 볼 수 있게)
        ensureWorkspaceEmailContact(mentor, workspace);
        ensureWorkspaceEmailContact(mentee, workspace);

        return workspace;
    }

    /**
     * 워크스페이스 상세 조회 (멤버 + 연락처 포함)
     */
    public WorkspaceDetailResponse getWorkspace(Long workspaceId) {
        User me = userService.getCurrentUser();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        boolean isMember = workspace.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(me.getId()));

        if (!isMember) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이 워크스페이스에 접근할 수 없습니다.");
        }

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

        Long workspaceId = workspace.getId();

        List<WorkspaceDetailResponse.Member> members = workspace.getMembers().stream()
                .map(m -> {
                    var user = m.getUser();

                    // 🔹 이 유저가 이 워크스페이스에서 볼 수 있는 연락처 가져오기
                    var contactInfos = contactInfoRepository
                            .findVisibleForWorkspace(user.getId(), workspaceId);

                    var contactDtos = contactInfos.stream()
                            .map(ci -> WorkspaceDetailResponse.Contact.builder()
                                    .type(ci.getType().name())
                                    .value(ci.getValue())
                                    .primary(ci.isPrimary())
                                    .build())
                            .toList();

                    return WorkspaceDetailResponse.Member.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .role(m.getRole())
                            .joinedAt(m.getJoinedAt())
                            .contacts(contactDtos)
                            .build();
                })
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

    /**
     * 워크스페이스 전용 EMAIL contact가 없으면 생성
     */
    private void ensureWorkspaceEmailContact(User user, Workspace workspace) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        boolean exists = contactInfoRepository
                .existsByUserIdAndTypeAndWorkspace_Id(user.getId(), ContactType.EMAIL, workspace.getId());
        if (exists) {
            return;
        }

        ContactInfo emailContact = ContactInfo.builder()
                .user(user)
                .workspace(workspace)               // ✅ 이 워크스페이스 전용
                .type(ContactType.EMAIL)
                .value(user.getEmail())
                .primary(true)                      // 이 워크스페이스 기준 primary
                .visibleToWorkspaceMembers(true)
                .build();

        contactInfoRepository.save(emailContact);
    }
}
