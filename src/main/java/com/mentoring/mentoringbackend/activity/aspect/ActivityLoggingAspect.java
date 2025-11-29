package com.mentoring.mentoringbackend.activity.aspect;

import com.mentoring.mentoringbackend.activity.domain.ActivityType;
import com.mentoring.mentoringbackend.activity.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 아주 심플한 버전:
 * - 모든 RestController 메서드 실행 후에
 *   "어떤 컨트롤러의 어떤 메서드가 호출되었는지" 정도만 로그로 남긴다.
 * - 나중에 필요하면 특정 컨트롤러/메서드만 선별해서 추적하도록 개선 가능.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final ActivityLogService activityLogService;

    /**
     * com.mentoring.mentoringbackend 하위의
     * @RestController 클래스들에 대한 포인트컷
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController com.mentoring.mentoringbackend..*)")
    public void restControllerMethods() {
    }

    @Around("restControllerMethods()")
    public Object logControllerActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String action = className + "#" + methodName;

            // 심플하게 OTHER 타입으로 남겨두고,
            // 나중에 필요하면 타입 분기 로직 추가
            activityLogService.logSimple(
                    ActivityType.OTHER,
                    action,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            // 로깅 실패가 실제 비즈니스 로직에 영향을 주지 않도록
            log.warn("Activity logging failed", e);
        }

        return result;
    }
}
