package com.mentoring.mentoringbackend.session.domain;

import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "session_attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(SessionAttendance.SessionAttendanceId.class)
public class SessionAttendance {

    @Id
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Id
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 20)
    private AttendanceStatus attendanceStatus;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    // ==== 편의 메서드 ====

    public void updateStatus(AttendanceStatus status, LocalDateTime checkedAt) {
        this.attendanceStatus = status;
        this.checkedAt = checkedAt;
    }

    /**
     * 복합 PK (session_id + user_id) 매핑용 ID 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionAttendanceId implements Serializable {

        private Long session; // Session PK
        private Long user;    // User PK
    }
}
