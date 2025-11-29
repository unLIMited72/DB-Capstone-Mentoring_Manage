package com.mentoring.mentoringbackend.tag.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagType type;

    @Column(name = "is_system", nullable = false)
    private Boolean system;

    @Column(name = "is_matchable", nullable = false)
    private Boolean matchable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tag_id")
    private Tag parentTag;   // MVP에선 null 가능

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 편의 메서드
    public void updateBasicInfo(String name, TagType type, Boolean matchable, String description) {
        this.name = name;
        this.type = type;
        this.matchable = matchable;
        this.description = description;
    }

    public void changeParent(Tag parentTag) {
        this.parentTag = parentTag;
    }
}
