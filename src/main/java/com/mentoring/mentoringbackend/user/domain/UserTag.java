package com.mentoring.mentoringbackend.user.domain;

import com.mentoring.mentoringbackend.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_tag_relation",
                        columnNames = {"user_id", "tag_id", "relation_type"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private RelationType relationType;

    @Column(nullable = false)
    private Integer level; // 1~5 정도

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void changeLevel(Integer level) {
        this.level = level;
    }
}
