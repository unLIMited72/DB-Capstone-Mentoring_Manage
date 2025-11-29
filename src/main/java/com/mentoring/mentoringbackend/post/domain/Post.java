package com.mentoring.mentoringbackend.post.domain;

import com.mentoring.mentoringbackend.academic.domain.Program;
import com.mentoring.mentoringbackend.tag.domain.PostTag;
import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    // 작성자
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 어떤 프로그램(한서튜터링 / 학습공동체 등)에 속하는 글인지
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 대상 수준 (예: "1~2학년 기초")
    @Column(name = "target_level", length = 100)
    private String targetLevel;

    // 최대 인원 (null이면 제한 없음)
    @Column(name = "max_members")
    private Integer maxMembers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    // 선택 확장 필드 (있어도 되고 없어도 됨)
    @Column(name = "expected_weeks")
    private Integer expectedWeeks;

    @Column(name = "expected_sessions_total")
    private Integer expectedSessionsTotal;

    @Column(name = "expected_sessions_per_week")
    private Integer expectedSessionsPerWeek;

    @Column(name = "preferred_mode", length = 50)
    private String preferredMode;

    @Column(name = "preferred_time_note", length = 255)
    private String preferredTimeNote;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostTag> postTags = new ArrayList<>();

    // ==== 편의 메서드 ====

    // 게시글 내용 수정용 메서드
    public void update(
        String title,
        String content,
        String targetLevel,
        Integer maxMembers,
        Integer expectedWeeks,
        Integer expectedSessionsTotal,
        Integer expectedSessionsPerWeek,
        String preferredMode,
        String preferredTimeNote
    ) {
        this.title = title;
        this.content = content;
        this.targetLevel = targetLevel;
        this.maxMembers = maxMembers;
        this.expectedWeeks = expectedWeeks;
        this.expectedSessionsTotal = expectedSessionsTotal;
        this.expectedSessionsPerWeek = expectedSessionsPerWeek;
        this.preferredMode = preferredMode;
        this.preferredTimeNote = preferredTimeNote;
    }

    public void addPostTag(PostTag postTag) {
        postTags.add(postTag);
    }

    public void changeStatus(PostStatus status) {
        this.status = status;
    }
}
