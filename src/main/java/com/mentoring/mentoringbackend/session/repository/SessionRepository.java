package com.mentoring.mentoringbackend.session.repository;

import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.session.domain.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    // 특정 워크스페이스의 세션들을 시간순으로 조회
    List<Session> findAllByWorkspaceIdOrderByScheduledAtAsc(Long workspaceId);


    long countByStatus(SessionStatus status);
}
