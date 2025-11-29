package com.mentoring.mentoringbackend.session.dto;

import com.mentoring.mentoringbackend.session.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionAttendanceRequest {

    @NotNull
    private Long userId;

    @NotNull
    private AttendanceStatus attendanceStatus;

    // 응답용 추가 필드
    private String userName;
    private LocalDateTime checkedAt;
}
