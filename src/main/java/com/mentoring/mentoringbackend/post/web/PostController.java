package com.mentoring.mentoringbackend.post.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.common.dto.PageResponse;
import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.post.dto.PostCreateRequest;
import com.mentoring.mentoringbackend.post.dto.PostResponse;
import com.mentoring.mentoringbackend.post.service.PostService;
import com.mentoring.mentoringbackend.post.dto.PostUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<PostResponse> createPost(
            @RequestBody @Valid PostCreateRequest request
    ) {
        return ApiResponse.success(postService.createPost(request));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.success(postService.getPost(postId));
    }

    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> listPosts(
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                postService.listPosts(programId, type, status, page, size)
        );
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        return ApiResponse.success(postService.updatePost(postId, request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.success(null);
    }    
}
