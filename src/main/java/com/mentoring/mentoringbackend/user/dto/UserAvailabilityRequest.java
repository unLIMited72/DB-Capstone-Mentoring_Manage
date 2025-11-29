package com.mentoring.mentoringbackend.user.dto;

import com.mentoring.mentoringbackend.user.domain.AvailabilityMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class UserAvailabilityRequest {

    @NotNull
    @Min(0)
    @Max(6)
    private Integer dayOfWeek; // 0(Sun) ~ 6(Sat)

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private AvailabilityMode mode;
}
