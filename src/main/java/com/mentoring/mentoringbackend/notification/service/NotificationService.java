package com.mentoring.mentoringbackend.notification.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.notification.domain.Notification;
import com.mentoring.mentoringbackend.notification.domain.NotificationStatus;
import com.mentoring.mentoringbackend.notification.domain.NotificationType;
import com.mentoring.mentoringbackend.notification.dto.NotificationResponse;
import com.mentoring.mentoringbackend.notification.repository.NotificationRepository;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 알림 목록 조회
     */
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = userService.getCurrentUser();
        List<Notification> notifications =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = userService.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "알림을 찾을 수 없습니다."
                ));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "다른 사용자의 알림입니다.");
        }

        notification.markRead();
    }

    /**
     * 즉시 알림 생성 (scheduledAt = now)
     */
    @Transactional
    public Notification createImmediateNotification(User user,
                                                    String title,
                                                    String message,
                                                    NotificationType type) {
        LocalDateTime now = LocalDateTime.now();

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.PENDING) // 생성 시점에는 PENDING
                .scheduledAt(now)
                .createdAt(now)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 예약 알림 생성 (예: D-1 리마인더)
     */
    @Transactional
    public Notification createScheduledNotification(User user,
                                                    String title,
                                                    String message,
                                                    NotificationType type,
                                                    LocalDateTime scheduledAt) {
        LocalDateTime now = LocalDateTime.now();

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.PENDING)
                .scheduledAt(scheduledAt)
                .createdAt(now)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 스케줄러에서 호출: PENDING + scheduledAt <= now 인 것들 SENT 처리
     * (실제 푸시/메일 발송은 추후 확장)
     */
    @Transactional
    public void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Notification> dueList =
                notificationRepository.findByStatusAndScheduledAtBefore(
                        NotificationStatus.PENDING, now
                );

        for (Notification notification : dueList) {
            notification.markSent(now);
            // 실제 발송 로직(카톡/메일 등)은 나중에 여기에 붙이면 됨
        }
    }
}
