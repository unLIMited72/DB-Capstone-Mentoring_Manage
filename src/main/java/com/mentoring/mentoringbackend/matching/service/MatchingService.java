package com.mentoring.mentoringbackend.matching.service;

import com.mentoring.mentoringbackend.academic.domain.Program;
import com.mentoring.mentoringbackend.academic.repository.ProgramRepository;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.matching.domain.MatchingConfig;
import com.mentoring.mentoringbackend.matching.domain.MatchSuggestion;
import com.mentoring.mentoringbackend.matching.dto.MatchingConfigRequest;
import com.mentoring.mentoringbackend.matching.dto.MatchingSuggestionResponse;
import com.mentoring.mentoringbackend.matching.repository.MatchingConfigRepository;
import com.mentoring.mentoringbackend.matching.repository.MatchSuggestionRepository;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.post.repository.PostRepository;
import com.mentoring.mentoringbackend.tag.domain.PostTag;
import com.mentoring.mentoringbackend.tag.repository.PostTagRepository;
import com.mentoring.mentoringbackend.user.domain.RelationType;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.domain.UserAvailability;
import com.mentoring.mentoringbackend.user.domain.UserTag;
import com.mentoring.mentoringbackend.user.repository.UserAvailabilityRepository;
import com.mentoring.mentoringbackend.user.repository.UserTagRepository;
import com.mentoring.mentoringbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final PostRepository postRepository;
    private final UserTagRepository userTagRepository;
    private final UserAvailabilityRepository userAvailabilityRepository;
    private final PostTagRepository postTagRepository;

    private final ProgramRepository programRepository;
    private final MatchingConfigRepository matchingConfigRepository;
    private final MatchSuggestionRepository matchSuggestionRepository;

    private final UserService userService;
    private final MatchingScoreCalculator scoreCalculator;

    /**
     * 현재 로그인한 사용자를 멘티 기준으로,
     * 해당 프로그램에서 열려 있는 멘토 모집글 추천
     */
    public List<MatchingSuggestionResponse> recommendMentorPosts(Long programId) {
        User mentee = userService.getCurrentUser();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "프로그램을 찾을 수 없습니다."));

        MatchingConfig config = matchingConfigRepository.findByProgramId(programId)
                .orElse(null);

        // 후보 게시글: 해당 프로그램 + type=MENTOR_RECRUIT + status=OPEN
        List<Post> candidates = postRepository.findAll().stream()
                .filter(post -> post.getProgram() != null
                        && post.getProgram().getId().equals(programId)
                        && post.getType() == PostType.MENTOR_RECRUIT
                        && post.getStatus() == PostStatus.OPEN)
                .toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        // 전체 user_tag / user_availability / post_tag 한 번에 로딩 후 메모리에서 필터 (MVP 용)
        List<UserTag> allUserTags = userTagRepository.findAll();
        List<UserAvailability> allAvailabilities = userAvailabilityRepository.findAll();
        List<PostTag> allPostTags = postTagRepository.findAll();

        // 멘티 태그 / 시간대
        List<UserTag> menteeTags = allUserTags.stream()
                .filter(ut -> ut.getUser().getId().equals(mentee.getId())
                        && (ut.getRelationType() == RelationType.WANT_TO_LEARN
                        || ut.getRelationType() == RelationType.INTEREST))
                .toList();
        Set<Long> menteeTagIds = menteeTags.stream()
                .map(ut -> ut.getTag().getId())
                .collect(Collectors.toSet());

        List<UserAvailability> menteeAvail = allAvailabilities.stream()
                .filter(av -> av.getUser().getId().equals(mentee.getId()))
                .toList();

        List<MatchingResult> results = new ArrayList<>();

        for (Post post : candidates) {
            User mentor = post.getAuthor();
            if (mentor == null) {
                continue;
            }

            // 멘토 태그
            List<UserTag> mentorTags = allUserTags.stream()
                    .filter(ut -> ut.getUser().getId().equals(mentor.getId())
                            && ut.getRelationType() == RelationType.CAN_TEACH)
                    .toList();
            Set<Long> mentorTagIds = mentorTags.stream()
                    .map(ut -> ut.getTag().getId())
                    .collect(Collectors.toSet());

            // 태그 유사도
            double tagScore = scoreCalculator.calculateTagScore(menteeTagIds, mentorTagIds);

            // 시간대 겹침 여부
            List<UserAvailability> mentorAvail = allAvailabilities.stream()
                    .filter(av -> av.getUser().getId().equals(mentor.getId()))
                    .toList();
            boolean hasOverlap = hasTimeOverlap(menteeAvail, mentorAvail);
            double timeScore = scoreCalculator.calculateTimeScore(hasOverlap);

            double totalScore = scoreCalculator.calculateTotalScore(tagScore, timeScore, config);
            double minScore = scoreCalculator.getMinScore(config);

            if (totalScore < minScore) {
                continue;
            }

            results.add(new MatchingResult(post, mentor, totalScore));
        }

        // 점수 내림차순 정렬 후 상위 N개 (예: 10개)만
        return results.stream()
                .sorted(Comparator.comparingDouble(MatchingResult::score).reversed())
                .limit(10)
                .map(r -> MatchingSuggestionResponse.builder()
                        .postId(r.post().getId())
                        .title(r.post().getTitle())
                        .type(r.post().getType())
                        .mentorId(r.mentor().getId())
                        .mentorName(r.mentor().getName())
                        .score(r.score())
                        .build())
                .toList();
    }

    /**
     * 프로그램별 매칭 설정 조회
     */
    public MatchingConfigRequest getConfig(Long programId) {
        MatchingConfig config = matchingConfigRepository.findByProgramId(programId)
                .orElse(null);

        MatchingConfigRequest dto = new MatchingConfigRequest();
        if (config == null) {
            dto.setWeightTag(0.7);
            dto.setWeightTime(0.3);
            dto.setMinScore(0.3);
        } else {
            dto.setWeightTag(config.getWeightTag());
            dto.setWeightTime(config.getWeightTime());
            dto.setMinScore(config.getMinScore());
        }
        return dto;
    }

    /**
     * 프로그램별 매칭 설정 저장/수정 (관리자용)
     */
    @Transactional
    public MatchingConfigRequest updateConfig(Long programId, MatchingConfigRequest request) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "프로그램을 찾을 수 없습니다."));

        MatchingConfig config = matchingConfigRepository.findByProgramId(programId)
                .orElseGet(() -> MatchingConfig.builder()
                        .program(program)
                        .weightTag(0.7)
                        .weightTime(0.3)
                        .minScore(0.3)
                        .build());

        config.update(request.getWeightTag(), request.getWeightTime(), request.getMinScore());
        matchingConfigRepository.save(config);

        return getConfig(programId);
    }

    /**
     * 단순 시간 겹침 체크
     * - 같은 요일 && 시간대가 조금이라도 겹치면 true
     */
    private boolean hasTimeOverlap(List<UserAvailability> a, List<UserAvailability> b) {
        for (UserAvailability ua : a) {
            for (UserAvailability ub : b) {
                if (!ua.getDayOfWeek().equals(ub.getDayOfWeek())) {
                    continue;
                }
                if (isTimeRangeOverlap(ua.getStartTime(), ua.getEndTime(), ub.getStartTime(), ub.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTimeRangeOverlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        return !e1.isBefore(s2) && !e2.isBefore(s1);
    }

    /**
     * 내부 계산용 record
     */
    private record MatchingResult(Post post, User mentor, double score) {}
}
