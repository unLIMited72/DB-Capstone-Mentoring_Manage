package com.mentoring.mentoringbackend.notification.repository;

import com.mentoring.mentoringbackend.notification.domain.Notification;
import com.mentoring.mentoringbackend.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내 알림 목록 (최신순)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 발송 대상 (PENDING + 시간이 지난 알림)
    List<Notification> findByStatusAndScheduledAtBefore(NotificationStatus status,
                                                        LocalDateTime beforeTime);
}
