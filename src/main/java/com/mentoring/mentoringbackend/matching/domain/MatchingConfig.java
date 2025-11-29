package com.mentoring.mentoringbackend.matching.domain;

import com.mentoring.mentoringbackend.academic.domain.Program;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "matching_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MatchingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_config_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    // 태그(관심/역량) 유사도 가중치
    @Column(name = "weight_tag", nullable = false)
    private double weightTag;

    // 시간대(availability) 겹침 가중치
    @Column(name = "weight_time", nullable = false)
    private double weightTime;

    // 최소 추천 점수 (이 값 이상만 추천 목록에 포함)
    @Column(name = "min_score", nullable = false)
    private double minScore;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(double weightTag, double weightTime, double minScore) {
        this.weightTag = weightTag;
        this.weightTime = weightTime;
        this.minScore = minScore;
    }
}
