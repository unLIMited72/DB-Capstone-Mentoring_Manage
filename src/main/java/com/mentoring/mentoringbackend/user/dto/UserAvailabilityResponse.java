package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.user.domain.AvailabilityMode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class UserAvailabilityResponse {

    private Long id;
    private Integer dayOfWeek;  // 0(Sun) ~ 6(Sat)
    private LocalTime startTime;
    private LocalTime endTime;
    private AvailabilityMode mode;
}
