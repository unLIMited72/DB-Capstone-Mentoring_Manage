package com.mentoring.mentoringbackend.user.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.user.domain.UserAvailability;
import com.mentoring.mentoringbackend.user.dto.UserAvailabilityRequest;
import com.mentoring.mentoringbackend.user.dto.UserAvailabilityResponse;
import com.mentoring.mentoringbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/availability")
@RequiredArgsConstructor
public class UserAvailabilityController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserAvailabilityResponse>> getMyAvailability() {
        List<UserAvailability> list = userService.getMyAvailability();
        List<UserAvailabilityResponse> dtoList = list.stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @PutMapping
    public ApiResponse<List<UserAvailabilityResponse>> updateMyAvailability(
            @RequestBody @Valid List<UserAvailabilityRequest> requests
    ) {
        List<UserAvailability> list = userService.updateMyAvailability(requests);
        List<UserAvailabilityResponse> dtoList = list.stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(dtoList);
    }

    private UserAvailabilityResponse toResponse(UserAvailability availability) {
        return UserAvailabilityResponse.builder()
                .id(availability.getId())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .mode(availability.getMode())
                .build();
    }
}
