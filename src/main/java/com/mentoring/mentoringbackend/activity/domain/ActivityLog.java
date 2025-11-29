package com.mentoring.mentoringbackend.activity.domain;

import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_log_id") // ✅ DB의 PK 컬럼명과 매핑
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 200)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * ActivityLogService / Aspect 에서 사용하기 위한 팩토리 메서드
     */
    public static ActivityLog create(
            User user,
            ActivityType type,
            String action,
            String resourceType,
            Long resourceId,
            String detail,
            String clientIp,
            String userAgent
    ) {
        return ActivityLog.builder()
                .userId(user != null ? user.getId() : null)
                .activityType(type)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .detail(detail)
                .ipAddress(clientIp)
                .userAgent(userAgent)
                .build();
    }
}
