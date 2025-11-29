package com.mentoring.mentoringbackend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * 5분마다 한 번씩,
     * PENDING 상태 + 시간이 지난 알림들을 SENT로 업데이트
     */
    @Scheduled(fixedDelayString = "300000") // 5분 = 300000ms
    public void processDueNotifications() {
        try {
            notificationService.sendDueNotifications();
        } catch (Exception e) {
            log.error("알림 발송 처리 중 에러 발생", e);
        }
    }
}
