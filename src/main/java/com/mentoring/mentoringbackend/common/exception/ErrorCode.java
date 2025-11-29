package com.mentoring.mentoringbackend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_BAD_REQUEST"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_VALIDATION_ERROR"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_INVALID_INPUT_VALUE"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_ENTITY_NOT_FOUND"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_UNAUTHORIZED"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_FORBIDDEN"),
    CONFLICT(HttpStatus.CONFLICT, "COMMON_CONFLICT"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_SERVER_ERROR"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_INVALID_REQUEST"), 

    // 🔹 인증/인가 관련
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED"),

    // 🔹 도메인 에러 (유저)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String code;
}
