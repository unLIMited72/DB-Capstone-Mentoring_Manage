package com.mentoring.mentoringbackend.activity.service;

import com.mentoring.mentoringbackend.activity.domain.ActivityLog;
import com.mentoring.mentoringbackend.activity.domain.ActivityType;
import com.mentoring.mentoringbackend.activity.repository.ActivityLogRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserService userService;

    /**
     * 가장 단순한 로깅 메서드 (Aspect에서 주로 사용)
     */
    @Transactional
    public void logSimple(
            ActivityType type,
            String action,
            String resourceType,
            Long resourceId,
            String detail
    ) {
        User currentUser = null;
        try {
            // 인증 안 된 경우 예외가 날 수 있으므로 try-catch
            currentUser = userService.getCurrentUser();
        } catch (Exception ignored) {
        }

        ActivityLog log = ActivityLog.create(
                currentUser,
                type,
                action,
                resourceType,
                resourceId,
                detail,
                null,
                null
        );

        activityLogRepository.save(log);
    }

    /**
     * IP, UA까지 포함해서 남기고 싶을 때 확장 버전
     */
    @Transactional
    public void logWithClientInfo(
            ActivityType type,
            String action,
            String resourceType,
            Long resourceId,
            String detail,
            String clientIp,
            String userAgent
    ) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception ignored) {
        }

        ActivityLog log = ActivityLog.create(
                currentUser,
                type,
                action,
                resourceType,
                resourceId,
                detail,
                clientIp,
                userAgent
        );

        activityLogRepository.save(log);
    }
}
