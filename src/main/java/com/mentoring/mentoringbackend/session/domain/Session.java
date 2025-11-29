package com.mentoring.mentoringbackend.session.domain;

import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "week_index")
    private Integer weekIndex;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 20, nullable = false)
    private SessionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SessionStatus status;

    // 선택 필드들 (없어도 되지만 있으면 회차 기록 복기에 좋음)
    @Column(columnDefinition = "TEXT")
    private String plan;              // 사전 계획

    @Column(columnDefinition = "TEXT")
    private String note;              // 진행 내용 요약

    @Column(name = "homework_summary", columnDefinition = "TEXT")
    private String homeworkSummary;   // 과제 요약

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ==== 편의 메서드 ====

    public void updateBasicInfo(
            String topic,
            LocalDateTime scheduledAt,
            SessionMode mode,
            Integer weekIndex
    ) {
        if (topic != null) this.topic = topic;
        if (scheduledAt != null) this.scheduledAt = scheduledAt;
        if (mode != null) this.mode = mode;
        if (weekIndex != null) this.weekIndex = weekIndex;
    }

    public void updateDetail(String plan, String note, String homeworkSummary) {
        if (plan != null) this.plan = plan;
        if (note != null) this.note = note;
        if (homeworkSummary != null) this.homeworkSummary = homeworkSummary;
    }

    public void changeStatus(SessionStatus status) {
        if (status != null) {
            this.status = status;
        }
    }
}
