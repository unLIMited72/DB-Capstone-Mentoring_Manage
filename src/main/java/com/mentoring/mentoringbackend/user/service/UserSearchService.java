package com.mentoring.mentoringbackend.user.service;

import com.mentoring.mentoringbackend.academic.domain.Major;
import com.mentoring.mentoringbackend.academic.repository.MajorRepository;
import com.mentoring.mentoringbackend.common.dto.PageResponse;
import com.mentoring.mentoringbackend.common.exception.CustomException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.dto.UserProfileResponse;
import com.mentoring.mentoringbackend.user.dto.UserSignupRequest;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입: UserSignupRequest -> User 엔티티 저장
     */
    @Transactional
    public User signup(UserSignupRequest request) {

        // 1) 이메일 중복 체크 (existsByEmail 메서드는 UserRepository에 선언 필요)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(
                    ErrorCode.CONFLICT,
                    "이미 사용 중인 이메일입니다."
            );
        }

        // 2) 학과(major) 조회
        Major major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.ENTITY_NOT_FOUND,
                                "해당 전공(major)을 찾을 수 없습니다."
                        )
                );

        // 3) User 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .studentId(request.getStudentId())
                .major(major)
                .role(request.getRole())
                .isActive(true)
                .build();

        // 4) 저장 후 리턴
        return userRepository.save(user);
    }

    public PageResponse<UserProfileResponse> listUsers(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var pageResult = userRepository.findAll(pageable);

        var content = pageResult.getContent().stream()
                .map(this::toProfileResponse)
                .toList();

        return PageResponse.of(content, pageResult);
    }

    // 🔹 로그인한 사용자 프로필 조회
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return toProfileResponse(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .studentId(user.getStudentId())
                .majorId(user.getMajor() != null ? user.getMajor().getId() : null)
                .majorName(user.getMajor() != null ? user.getMajor().getName() : null)
                .role(user.getRole())
                .active(Boolean.TRUE.equals(user.getIsActive()))
                .build();
    }
}
