package com.mentoring.mentoringbackend.post.repository;

import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.domain.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByProgramId(Long programId, Pageable pageable);

    Page<Post> findAllByProgramIdAndType(Long programId, PostType type, Pageable pageable);

    Page<Post> findAllByProgramIdAndTypeAndStatus(
            Long programId,
            PostType type,
            PostStatus status,
            Pageable pageable
    );

    long countByStatus(PostStatus status);
}
