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
import com.mentoring.mentoringbackend.post.domain.ApplicationStatus;
import com.mentoring.mentoringbackend.post.domain.Post;
import com.mentoring.mentoringbackend.post.domain.PostStatus;
import com.mentoring.mentoringbackend.post.domain.PostType;
import com.mentoring.mentoringbackend.post.repository.PostApplicationRepository;
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
    private final PostApplicationRepository postApplicationRepository;

    /**
     * 현재 로그인한 사용자를 멘티 기준으로,
     * 해당 프로그램에서 열려 있는 멘토 모집글 추천
     */
    @Transactional  // 🔹 match_suggestion 기록 저장을 위해 readOnly=false
    public List<MatchingSuggestionResponse> recommendMentorPosts(Long programId) {
        User mentee = userService.getCurrentUser();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "프로그램을 찾을 수 없습니다."));

        MatchingConfig config = matchingConfigRepository.findByProgramId(programId)
                .orElse(null);

        // 🔹 후보 게시글: 같은 프로그램 + 멘토 모집글 + 정원이 남아 있는 글
        List<Post> candidates = postRepository.findAll().stream()
                .filter(post -> post.getProgram() != null
                        && post.getProgram().getId().equals(programId)
                        && post.getType() == PostType.MENTOR_RECRUIT)
                .filter(this::isRecruitingPost)              // OPEN / MATCHED + 정원 미달
                .filter(post -> !post.getAuthor().getId().equals(mentee.getId())) // 자기 글 제외
                .toList();

        // 후보가 하나도 없으면: 기존 로그 삭제 후 빈 리스트 반환
        if (candidates.isEmpty()) {
            matchSuggestionRepository.deleteByProgramIdAndMenteeId(programId, mentee.getId());
            return List.of();
        }

        // 🔹 전체 태그/시간/포스트태그 미리 로딩 (MVP 용)
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

            // 🔹 멘토 태그 (CAN_TEACH)
            List<UserTag> mentorTags = allUserTags.stream()
                    .filter(ut -> ut.getUser().getId().equals(mentor.getId())
                            && ut.getRelationType() == RelationType.CAN_TEACH)
                    .toList();
            Set<Long> mentorTagIds = mentorTags.stream()
                    .map(ut -> ut.getTag().getId())
                    .collect(Collectors.toSet());

            // 🔹 게시글 태그 (post_tag)
            Set<Long> postTagIds = getPostTagIds(allPostTags, post.getId());

            // 🔹 멘토-멘티 태그 유사도
            double mentorTagScore = scoreCalculator.calculateTagScore(menteeTagIds, mentorTagIds);
            // 🔹 멘티-게시글 태그 유사도
            double postTagScore = scoreCalculator.calculateTagScore(menteeTagIds, postTagIds);
            // 🔹 최종 태그 점수 (멘토 70%, 게시글 30%)
            double tagScore = (mentorTagScore * 0.7) + (postTagScore * 0.3);

            // 🔹 시간/모드 겹침
            List<UserAvailability> mentorAvail = allAvailabilities.stream()
                    .filter(av -> av.getUser().getId().equals(mentor.getId()))
                    .toList();
            boolean hasOverlap = hasTimeOverlap(menteeAvail, mentorAvail);
            boolean hasStrongOverlap = hasTimeAndModeOverlap(menteeAvail, mentorAvail);

            double timeAndModeScore;
            if (!hasOverlap) {
                timeAndModeScore = 0.0;      // 시간도 안 겹치면 0
            } else if (hasStrongOverlap) {
                timeAndModeScore = 1.0;      // 요일 + 시간 + 모드까지 잘 맞음
            } else {
                timeAndModeScore = 0.7;      // 시간만 겹치고, 모드는 다름
            }

            // 🔹 전공 점수
            double majorScore = 0.0;
            if (mentor.getMajor() != null && mentee.getMajor() != null &&
                    mentor.getMajor().getId().equals(mentee.getMajor().getId())) {
                majorScore = 1.0;
            }

            // 🔹 시간/모드 + 전공을 합친 timeScore
            double timeScore = 0.6 * timeAndModeScore + 0.4 * majorScore;

            // 🔹 최종 점수
            double totalScore = scoreCalculator.calculateTotalScore(tagScore, timeScore, config);
            double minScore = scoreCalculator.getMinScore(config);

            if (totalScore < minScore) {
                continue;
            }

            results.add(new MatchingResult(post, mentor, totalScore));
        }

        // 🔹 점수 내림차순 + 상위 2개만 사용
        List<MatchingResult> topResults = results.stream()
                .sorted(Comparator.comparingDouble(MatchingResult::score).reversed())
                .limit(2)
                .toList();

        // 기존 로그 삭제
        matchSuggestionRepository.deleteByProgramIdAndMenteeId(programId, mentee.getId());

        if (topResults.isEmpty()) {
            return List.of();
        }

        // 🔹 match_suggestion 로그 저장
        List<MatchSuggestion> entities = topResults.stream()
                .map(r -> MatchSuggestion.builder()
                        .program(program)
                        .mentee(mentee)
                        .post(r.post())
                        .score(r.score())
                        .build())
                .toList();

        matchSuggestionRepository.saveAll(entities);

        // 🔹 클라이언트 반환 DTO
        return topResults.stream()
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

    // ====== 내부 헬퍼들 ======

    // 🔹 해당 post가 아직 정원이 남아 있는지 확인
    private boolean isRecruitingPost(Post post) {
        // maxMembers 가 null 이면 그냥 OPEN 인 경우만 받도록 (안전장치)
        Integer maxMembers = post.getMaxMembers();
        if (maxMembers == null || maxMembers <= 0) {
            return post.getStatus() == PostStatus.OPEN;
        }

        long acceptedCount = postApplicationRepository.countByPostIdAndStatus(
                post.getId(), ApplicationStatus.ACCEPTED
        );

        // OPEN 또는 MATCHED 이면서 정원이 남아 있으면 true
        return (post.getStatus() == PostStatus.OPEN || post.getStatus() == PostStatus.MATCHED)
                && acceptedCount < maxMembers;
    }

    // 🔹 특정 post에 달린 tag ID 집합
    private Set<Long> getPostTagIds(List<PostTag> allPostTags, Long postId) {
        return allPostTags.stream()
                .filter(pt -> pt.getPost().getId().equals(postId))
                .map(pt -> pt.getTag().getId())
                .collect(Collectors.toSet());
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
                if (isTimeRangeOverlap(ua.getStartTime(), ua.getEndTime(),
                        ub.getStartTime(), ub.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 요일 + 시간 + mode(ONLINE/OFFLINE) 까지 겹치는지 체크
     */
    private boolean hasTimeAndModeOverlap(List<UserAvailability> a, List<UserAvailability> b) {
        for (UserAvailability ua : a) {
            for (UserAvailability ub : b) {
                if (!ua.getDayOfWeek().equals(ub.getDayOfWeek())) {
                    continue;
                }
                if (!ua.getMode().equals(ub.getMode())) {
                    continue;
                }
                if (isTimeRangeOverlap(ua.getStartTime(), ua.getEndTime(),
                        ub.getStartTime(), ub.getEndTime())) {
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
