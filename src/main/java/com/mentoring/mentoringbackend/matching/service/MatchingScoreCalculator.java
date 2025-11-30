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
    * 전체 점수 = 태그 영역 점수 * weightTag + (시간/모드/전공 등 비태그 영역 점수) * weightTime
    *  - tagScore  : 태그 기반 유사도 (멘티-멘토, 멘티-게시글 태그 조합)
    *  - timeScore : 시간/모드/전공 등을 종합한 점수 (0.0 ~ 1.0 범위 권장)
    * config 가 null 이면 기본값 사용 (태그 0.7, 비태그 0.3, minScore 0.3)
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
