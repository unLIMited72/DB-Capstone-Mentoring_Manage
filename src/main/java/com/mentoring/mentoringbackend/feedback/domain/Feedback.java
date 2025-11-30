package com.mentoring.mentoringbackend.feedback.domain;
import com.mentoring.mentoringbackend.feedback.domain.FeedbackTargetType;
import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id")
    private Session session; // NULL 가능 (전체 워크스페이스 평가)

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    // ✅ 추가: 평가 대상 유저 (멘토/멘티, 전체 평가면 null)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    // ✅ 추가: 어떤 타입의 평가인지
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private FeedbackTargetType targetType;

    @Column(nullable = false)
    private Integer rating;   // 1~5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === 편의 메서드 ===
    public void update(Integer rating, String comment, Boolean anonymous, Session session) {
        if (rating != null) this.rating = rating;
        if (comment != null) this.comment = comment;
        if (anonymous != null) this.anonymous = anonymous;
        if (session != null) this.session = session;
        // targetType / toUser는 생성 시 고정(보통 수정 안 하는게 자연스러움)
    }
}