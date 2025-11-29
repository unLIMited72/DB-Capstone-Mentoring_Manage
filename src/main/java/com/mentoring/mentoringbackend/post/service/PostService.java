package com.mentoring.mentoringbackend.post.service;

import com.mentoring.mentoringbackend.academic.domain.Program;
import com.mentoring.mentoringbackend.academic.repository.ProgramRepository;
import com.mentoring.mentoringbackend.common.dto.PageResponse;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.common.util.PageableUtils;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.post.dto.PostCreateRequest;
import com.mentoring.mentoringbackend.post.dto.PostResponse;
import com.mentoring.mentoringbackend.post.dto.PostUpdateRequest;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.tag.domain.PostTag;
import com.mentoring.mentoringbackend.tag.domain.Tag;
import com.mentoring.mentoringbackend.tag.dto.TagDto;
import com.mentoring.mentoringbackend.tag.repository.PostTagRepository;
import com.mentoring.mentoringbackend.tag.repository.TagRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final ProgramRepository programRepository;
    private final UserService userService;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    // ===== 게시글 생성 =====
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        User author = userService.getCurrentUser();
        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "프로그램을 찾을 수 없습니다."));

        Post post = Post.builder()
                .author(author)
                .program(program)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .targetLevel(request.getTargetLevel())
                .maxMembers(request.getMaxMembers())
                .status(PostStatus.OPEN)
                .expectedWeeks(request.getExpectedWeeks())
                .expectedSessionsTotal(request.getExpectedSessionsTotal())
                .expectedSessionsPerWeek(request.getExpectedSessionsPerWeek())
                .preferredMode(request.getPreferredMode())
                .preferredTimeNote(request.getPreferredTimeNote())
                .build();

        Post saved = postRepository.save(post);

        // 태그 연결
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            List<PostTag> postTags = tags.stream()
                    .map(tag -> PostTag.builder()
                            .post(saved)
                            .tag(tag)
                            .build())
                    .toList();

            postTagRepository.saveAll(postTags);
            // 양방향 관계 동기화 (선택이지만 깔끔하게)
            saved.getPostTags().addAll(postTags);
        }

        return toResponse(saved);
    }

    // ===== 게시글 수정 =====
@Transactional
public PostResponse updatePost(Long postId, PostUpdateRequest request) {
    User me = userService.getCurrentUser();
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시글을 찾을 수 없습니다."));

    // 작성자만 수정 가능
    if (!post.getAuthor().getId().equals(me.getId())) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "본인의 글만 수정할 수 있습니다.");
    }

    // ====== 기본 필드 수정 ======
    post.update(
            request.getTitle(),
            request.getContent(),
            request.getTargetLevel(),
            request.getMaxMembers(),
            request.getExpectedWeeks(),
            request.getExpectedSessionsTotal(),
            request.getExpectedSessionsPerWeek(),
            request.getPreferredMode(),
            request.getPreferredTimeNote()
    );
    // 또는 setter로 직접 하나씩 넣어줘도 됨

    // ====== 태그 교체 로직 (중복 방지) ======
    if (request.getTagIds() != null) {
        // 요청으로 들어온 태그 id 집합
        Set<Long> newTagIds = new HashSet<>(request.getTagIds());

        // 1) 기존 PostTag 중에서, 요청에 없는 태그는 제거
        Iterator<PostTag> iterator = post.getPostTags().iterator();
        while (iterator.hasNext()) {
            PostTag pt = iterator.next();
            Long existingTagId = pt.getTag().getId();

            if (!newTagIds.contains(existingTagId)) {
                // 요청에 없는 태그 → 제거(고아 처리)
                iterator.remove();
                // 양방향 매핑이면 pt.setPost(null); 같은 것도 선택적으로 해줄 수 있음
            } else {
                // 요청에도 이미 있는 태그 → 그대로 두고, 새로 만들 필요 없으니 set에서 제거
                newTagIds.remove(existingTagId);
            }
        }

        // 2) 남아 있는 newTagIds는 "새로 추가해야 할 태그"들
        if (!newTagIds.isEmpty()) {
            List<Tag> tagsToAdd = tagRepository.findAllById(newTagIds);
            for (Tag tag : tagsToAdd) {
                PostTag postTag = PostTag.builder()
                        .post(post)
                        .tag(tag)
                        .build();
                post.addPostTag(postTag);  // cascade = ALL 이므로 flush 시 자동 insert
            }
        }
    } else {
        // tagIds가 null이면 태그를 다 지울지, 그대로 둘지는 정책에 따라
        // 모두 삭제하려면:
        // post.getPostTags().clear();
    }

    // post는 영속 상태라 별도의 save() 없이도 변경 감지 + cascade 됨
    return toResponse(post);
}


    // ===== 게시글 삭제 =====
@Transactional
public void deletePost(Long postId) {
    User me = userService.getCurrentUser();

    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시글을 찾을 수 없습니다."));

    // 1) 작성자 본인만 삭제 가능
    if (!post.getAuthor().getId().equals(me.getId())) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "본인이 작성한 글만 삭제할 수 있습니다.");
    }

    // 2) 이미 매칭된 글은 삭제 제한 (원하면 메시지 바꿔도 됨)
    if (post.getStatus() == PostStatus.MATCHED) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "이미 매칭된 글은 삭제(마감)할 수 없습니다.");
    }

    // 3) 실제 DB DELETE 대신 상태를 CLOSED로 변경 (soft delete)
    post.changeStatus(PostStatus.CLOSED);
}


    // ===== 단건 조회 =====
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "게시글을 찾을 수 없습니다."));
        return toResponse(post);
    }

    // ===== 목록 조회 (프로그램/타입/상태 필터 지원) =====
    public PageResponse<PostResponse> listPosts(
            Long programId,
            PostType type,
            PostStatus status,
            int page,
            int size
    ) {
        Page<Post> result;

        if (programId != null && type != null && status != null) {
            result = postRepository.findAllByProgramIdAndTypeAndStatus(
                    programId, type, status, PageableUtils.of(page, size)
            );
        } else if (programId != null && type != null) {
            result = postRepository.findAllByProgramIdAndType(
                    programId, type, PageableUtils.of(page, size)
            );
        } else if (programId != null) {
            result = postRepository.findAllByProgramId(
                    programId, PageableUtils.of(page, size)
            );
        } else {
            result = postRepository.findAll(PageableUtils.of(page, size));
        }

        Page<PostResponse> mapped = result.map(this::toResponse);
        return PageableUtils.toPageResponse(mapped);
    }

    // ===== 엔티티 -> DTO 변환 =====
    private PostResponse toResponse(Post post) {
        List<TagDto> tagDtos = post.getPostTags().stream()
                .map(pt -> {
                    Tag t = pt.getTag();
                    return TagDto.builder()
                            .id(t.getId())
                            .name(t.getName())
                            .type(t.getType())
                            .system(Boolean.TRUE.equals(t.getSystem()))
                            .matchable(Boolean.TRUE.equals(t.getMatchable()))
                            .parentTagId(t.getParentTag() != null ? t.getParentTag().getId() : null)
                            .parentTagName(t.getParentTag() != null ? t.getParentTag().getName() : null)
                            .description(t.getDescription())
                            .build();
                })
                .toList();

        return PostResponse.builder()
                .id(post.getId())
                .programId(post.getProgram().getId())
                .programName(post.getProgram().getName())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getName())
                .type(post.getType())
                .status(post.getStatus())
                .title(post.getTitle())
                .content(post.getContent())
                .targetLevel(post.getTargetLevel())
                .maxMembers(post.getMaxMembers())
                .expectedWeeks(post.getExpectedWeeks())
                .expectedSessionsTotal(post.getExpectedSessionsTotal())
                .expectedSessionsPerWeek(post.getExpectedSessionsPerWeek())
                .preferredMode(post.getPreferredMode())
                .preferredTimeNote(post.getPreferredTimeNote())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(tagDtos)
                .build();
    }
}
