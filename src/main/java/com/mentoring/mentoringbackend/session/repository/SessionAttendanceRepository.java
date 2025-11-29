package com.mentoring.mentoringbackend.session.repository;

import com.mentoring.mentoringbackend.session.domain.SessionAttendance;
import com.mentoring.mentoringbackend.session.domain.SessionAttendance.SessionAttendanceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, SessionAttendanceId> {

    List<SessionAttendance> findAllBySessionId(Long sessionId);

    Optional<SessionAttendance> findBySessionIdAndUserId(Long sessionId, Long userId);
}
