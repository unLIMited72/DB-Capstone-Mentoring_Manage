package com.mentoring.mentoringbackend.common.exception;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException; // 🔹 import 추가

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 우리가 직접 던지는 CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("CustomException: {} - {}", errorCode.getCode(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode, ex.getMessage()));
    }

    // 🔹 @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("입력 값이 올바르지 않습니다.");

        log.warn("Validation error: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    // 🔹 그 밖의 모든 예외 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "알 수 없는 오류가 발생했습니다."
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthenticationException ex) {
    log.warn("Authentication failed", ex);

    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(
                    ErrorCode.AUTH_UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            ));
}

}

