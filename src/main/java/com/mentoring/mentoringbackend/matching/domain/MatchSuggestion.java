package com.mentoring.mentoringbackend.matching.domain;

import com.mentoring.mentoringbackend.academic.domain.Program;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "match_suggestion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MatchSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_suggestion_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    // 추천을 받은 쪽 (보통 멘티)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    // 추천 대상 게시글 (멘토 모집글)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "score", nullable = false)
    private double score;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
