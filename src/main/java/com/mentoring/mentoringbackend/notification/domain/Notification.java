package com.mentoring.mentoringbackend.notification.domain;

import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 대상 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 간단 제목
    @Column(nullable = false, length = 200)
    private String title;

    // 상세 메시지
    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    // 예정 발송 시각 (null이면 즉시)
    private LocalDateTime scheduledAt;

    // 실제 발송된 시각
    private LocalDateTime sentAt;

    // 생성 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ===== 편의 메서드 =====

    public void markSent(LocalDateTime now) {
        this.status = NotificationStatus.SENT;
        this.sentAt = now;
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
    }
}
