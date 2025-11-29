package com.mentoring.mentoringbackend.post.repository;

import com.mentoring.mentoringbackend.post.domain.ApplicationStatus;
import com.mentoring.mentoringbackend.post.domain.PostApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostApplicationRepository extends JpaRepository<PostApplication, Long> {

    List<PostApplication> findAllByPostId(Long postId);

    List<PostApplication> findAllByFromUserIdOrderByCreatedAtDesc(Long fromUserId);

    List<PostApplication> findAllByToUserIdOrderByCreatedAtDesc(Long toUserId);

    long countByPostIdAndStatus(Long postId, ApplicationStatus status);

    boolean existsByPostIdAndFromUserIdAndToUserIdAndStatus(
        Long postId,
        Long fromUserId,
        Long toUserId,
        ApplicationStatus status
    );
}
