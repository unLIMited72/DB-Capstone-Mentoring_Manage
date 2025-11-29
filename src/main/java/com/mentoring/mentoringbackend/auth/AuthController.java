package com.mentoring.mentoringbackend.auth;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.common.exception.CustomException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.dto.UserLoginRequest;
import com.mentoring.mentoringbackend.user.dto.UserSignupRequest;
import com.mentoring.mentoringbackend.user.dto.UserProfileResponse;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.service.UserSearchService;
import com.mentoring.mentoringbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

// 회원가입 + 조회 둘 다 UserSearchService로 처리
private final UserSearchService userSearchService;
private final UserRepository userRepository;        // 로그인 시 유저 조회
private final PasswordEncoder passwordEncoder;      // 비밀번호 비교
private final JwtTokenProvider jwtTokenProvider;    // JWT 생성

    /**
     * 회원가입
     */
@PostMapping("/signup")
public ApiResponse<UserProfileResponse> signup(
        @Valid @RequestBody UserSignupRequest request
) {
    // 🔹 이제 UserSearchService 안의 signup 사용
    User user = userSearchService.signup(request);

    UserProfileResponse profile = UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .studentId(user.getStudentId())
            .majorId(user.getMajor() != null ? user.getMajor().getId() : null)
            .majorName(user.getMajor() != null ? user.getMajor().getName() : null)
            .role(user.getRole())
            .active(Boolean.TRUE.equals(user.getIsActive()))
            .build();

    return ApiResponse.success(profile);
}


    /**
     * 로그인 (이메일/비밀번호 수동 검증 + JWT 발급)
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        // 1) 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.AUTH_UNAUTHORIZED,
                                "이메일 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        // 2) 활성화 여부 체크 (선택)
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new CustomException(
                    ErrorCode.AUTH_UNAUTHORIZED,
                    "비활성화된 계정입니다."
            );
        }

        // 3) 비밀번호 일치 여부 체크
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException(
                    ErrorCode.AUTH_UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 4) 토큰 생성
        String accessToken = jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .build();

        return ApiResponse.success(response);
    }
}
