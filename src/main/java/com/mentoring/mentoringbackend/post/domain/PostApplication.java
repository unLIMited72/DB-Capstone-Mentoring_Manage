package com.mentoring.mentoringbackend.post.domain;

import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "post_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PostApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 신청 보낸 사람
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    // 신청 받는 사람 (보통 글 작성자)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    // ==== 편의 메서드 ====
    public void decide(ApplicationStatus status, LocalDateTime decidedAt) {
        this.status = status;
        this.decidedAt = decidedAt;
    }
}
