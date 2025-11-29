package com.mentoring.mentoringbackend.common.dto;

import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .errorCode(null)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .message(null)
                .errorCode(null)
                .build();
    }

    public static ApiResponse<Void> error(ErrorCode code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .data(null)
                .message(message)
                .errorCode(code != null ? code.getCode() : null)
                .build();
    }
}
