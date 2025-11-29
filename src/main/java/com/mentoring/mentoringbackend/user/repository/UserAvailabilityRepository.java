package com.mentoring.mentoringbackend.user.repository;

import com.mentoring.mentoringbackend.user.domain.UserAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAvailabilityRepository extends JpaRepository<UserAvailability, Long> {

    List<UserAvailability> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
