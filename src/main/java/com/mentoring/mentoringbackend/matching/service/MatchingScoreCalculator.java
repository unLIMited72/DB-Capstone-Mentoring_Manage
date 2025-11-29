package com.mentoring.mentoringbackend.matching.service;

import com.mentoring.mentoringbackend.matching.domain.MatchingConfig;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MatchingScoreCalculator {

    /**
     * 태그 유사도 (Jaccard 유사도 간단 버전)
     */
    public double calculateTagScore(Set<Long> menteeTagIds, Set<Long> mentorTagIds) {
        if (menteeTagIds.isEmpty() || mentorTagIds.isEmpty()) {
            return 0.0;
        }

        long intersection = menteeTagIds.stream()
                .filter(mentorTagIds::contains)
                .count();

        long union = menteeTagIds.size() + mentorTagIds.size() - intersection;
        if (union <= 0) {
            return 0.0;
        }

        return (double) intersection / union;
    }

    /**
     * 시간대 겹침 점수
     * - hasOverlap=true 이면 1.0
     * - 아니면 0.0
     */
    public double calculateTimeScore(boolean hasOverlap) {
        return hasOverlap ? 1.0 : 0.0;
    }

    /**
     * 전체 점수 = 태그*가중치 + 시간*가중치
     * config 가 null 이면 기본값 사용 (태그 0.7, 시간 0.3, minScore 0.3)
     */
    public double calculateTotalScore(double tagScore, double timeScore, MatchingConfig config) {
        double weightTag = 0.7;
        double weightTime = 0.3;

        if (config != null) {
            double sum = config.getWeightTag() + config.getWeightTime();
            if (sum > 0) {
                weightTag = config.getWeightTag() / sum;
                weightTime = config.getWeightTime() / sum;
            }
        }

        return (tagScore * weightTag) + (timeScore * weightTime);
    }

    public double getMinScore(MatchingConfig config) {
        if (config == null) {
            return 0.3;
        }
        return config.getMinScore();
    }
}
