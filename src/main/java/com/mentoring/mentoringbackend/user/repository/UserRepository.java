package com.mentoring.mentoringbackend.user.repository;

import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    long countByRole(Role role);
}
