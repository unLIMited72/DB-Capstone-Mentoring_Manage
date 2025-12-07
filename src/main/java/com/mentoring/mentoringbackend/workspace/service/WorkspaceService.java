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
import com.mentoring.mentoringbackend.user.domain.Role;
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
     * Create a workspace manually by admins/operators.
     */
    @Transactional
    public WorkspaceDetailResponse createWorkspace(WorkspaceCreateRequest request) {

        User creator = userService.getCurrentUser();

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Program not found."));

        Post sourcePost = null;
        if (request.getSourcePostId() != null) {
            sourcePost = postRepository.findById(request.getSourcePostId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Post not found."));
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

        // Register members
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

        // If no member specified �� creator becomes default mentor
        if (toSaveMembers.isEmpty()) {
            toSaveMembers.add(buildMember(saved, creator, WorkspaceRole.MENTOR));
        }

        workspaceMemberRepository.saveAll(toSaveMembers);

        return toDetailResponse(saved);
    }

    /**
     * Create workspace automatically when a PostApplication is ACCEPTED.
     * If an ACTIVE workspace already exists �� reuse it.
     * Otherwise �� create a new one.
     * Supports mentor 1 : N mentees.
     */
    @Transactional
    public Workspace createWorkspaceFromAcceptedApplication(PostApplication application) {

        Post post = application.getPost();
        Program program = post.getProgram();

        // 1) Find an existing ACTIVE workspace for this program + post
        Workspace workspace = workspaceRepository
                .findByProgramIdAndSourcePostIdAndStatus(
                        program.getId(),
                        post.getId(),
                        WorkspaceStatus.ACTIVE
                )
                .orElseGet(() -> {
                    // If none exists �� create a new workspace
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

        // 2) Determine mentor and mentee depending on post type
        User mentor;
        User mentee;

        if (post.getType() == PostType.MENTOR_RECRUIT) {
            mentor = post.getAuthor();
            mentee = application.getFromUser();
        } else {
            mentor = application.getFromUser();
            mentee = post.getAuthor();
        }

        // 3) Register members if not already added
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), mentor.getId())) {
            workspaceMemberRepository.save(buildMember(workspace, mentor, WorkspaceRole.MENTOR));
        }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), mentee.getId())) {
            workspaceMemberRepository.save(buildMember(workspace, mentee, WorkspaceRole.MENTEE));
        }

        // 4) Ensure workspace-specific email visibility
        ensureWorkspaceEmailContact(mentor, workspace);
        ensureWorkspaceEmailContact(mentee, workspace);

        return workspace;
    }

    /**
     * Get workspace detail ? only members can access.
     */
    public WorkspaceDetailResponse getWorkspace(Long workspaceId) {
    User me = userService.getCurrentUser();

    Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Workspace not found."));

    boolean isMember = workspace.getMembers().stream()
            .anyMatch(m -> m.getUser().getId().equals(me.getId()));
    boolean isAdmin = me.getRole() == Role.ADMIN;

    // Only members can see the workspace, except ADMIN who can see everything
    if (!isMember && !isAdmin) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "You are not allowed to access this workspace.");
    }

    return toDetailResponse(workspace);
    }


    /**
     * Get only the workspaces the current user belongs to.
     */
    public List<WorkspaceSummaryResponse> getMyWorkspaces() {

        User me = userService.getCurrentUser();

        List<WorkspaceMember> memberships = workspaceMemberRepository.findAllByUserId(me.getId());

        List<Workspace> workspaces = memberships.stream()
                .map(WorkspaceMember::getWorkspace)
                .distinct()
                .collect(Collectors.toList());

        return workspaces.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    /**
     * Admin: list all workspaces in the system.
     */
    public List<WorkspaceSummaryResponse> getAllWorkspacesForAdmin() {
        List<Workspace> workspaces = workspaceRepository.findAll();

        return workspaces.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    /**
     * ADMIN ONLY ? Get all workspaces in the system.
     */
    public List<WorkspaceSummaryResponse> getAllWorkspaces() {

        List<Workspace> workspaces = workspaceRepository.findAll();

        return workspaces.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    // ======================================================
    // Internal utility methods
    // ======================================================

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

                    // Fetch contact information visible within this workspace
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
     * Create workspace-specific email contact if it does not already exist.
     */
    private void ensureWorkspaceEmailContact(User user, Workspace workspace) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        boolean exists = contactInfoRepository
                .existsByUserIdAndTypeAndWorkspace_Id(user.getId(), ContactType.EMAIL, workspace.getId());

        if (exists) return;

        ContactInfo emailContact = ContactInfo.builder()
                .user(user)
                .workspace(workspace)
                .type(ContactType.EMAIL)
                .value(user.getEmail())
                .primary(true)
                .visibleToWorkspaceMembers(true)
                .build();

        contactInfoRepository.save(emailContact);
    }
}
