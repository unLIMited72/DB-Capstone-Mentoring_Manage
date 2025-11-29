package com.mentoring.mentoringbackend.user.repository;

import com.mentoring.mentoringbackend.user.domain.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    List<UserTag> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
