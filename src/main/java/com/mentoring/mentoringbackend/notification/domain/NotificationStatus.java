package com.mentoring.mentoringbackend.notification.domain;

public enum NotificationStatus {

    PENDING,    // 아직 발송되지 않음 (스케줄 대기)
    SENT,       // 발송 처리 완료 (UI에 노출 가능)
    READ        // 사용자가 읽음 처리함
}
