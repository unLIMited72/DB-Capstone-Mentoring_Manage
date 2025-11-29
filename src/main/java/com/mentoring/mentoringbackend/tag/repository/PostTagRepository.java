package com.mentoring.mentoringbackend.tag.repository;

import com.mentoring.mentoringbackend.tag.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from PostTag pt where pt.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
