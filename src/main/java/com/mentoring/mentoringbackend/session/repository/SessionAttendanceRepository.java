package com.mentoring.mentoringbackend.session.repository;

import com.mentoring.mentoringbackend.session.domain.AttendanceStatus;
import com.mentoring.mentoringbackend.session.domain.SessionAttendance;
import com.mentoring.mentoringbackend.session.domain.SessionAttendance.SessionAttendanceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, SessionAttendanceId> {

    // ✅ Session 엔티티의 id를 기준으로 조회
    //    SessionAttendance.session.id 를 따라가기 때문에 _ 로 연결해줘야 함
    List<SessionAttendance> findAllBySession_Id(Long sessionId);

    // ✅ Session.id + User.id 로 조회
    Optional<SessionAttendance> findBySession_IdAndUser_Id(Long sessionId, Long userId);

    // ✅ 엔티티 필드 이름에 맞게 변경 (attendanceStatus)
    long countByAttendanceStatus(AttendanceStatus attendanceStatus); 
}
