package com.mentoring.mentoringbackend.matching.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MatchingConfigRequest {

    @Min(0)
    @Max(1)
    private double weightTag;      // 0.0 ~ 1.0

    @Min(0)
    @Max(1)
    private double weightTime;     // 0.0 ~ 1.0

    @PositiveOrZero
    private double minScore;       // 0 이상

    // 혹시 합이 1이 아닐 수 있으니, Service 쪽에서 normalize 해줄 예정
}
