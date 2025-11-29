package com.mentoring.mentoringbackend.notification.dto;

import com.mentoring.mentoringbackend.notification.domain.Notification;
import com.mentoring.mentoringbackend.notification.domain.NotificationStatus;
import com.mentoring.mentoringbackend.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .status(notification.getStatus())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
