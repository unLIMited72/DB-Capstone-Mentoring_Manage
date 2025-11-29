package com.mentoring.mentoringbackend.user.service;

import com.mentoring.mentoringbackend.auth.SecurityUserDetails;
import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.user.domain.ContactInfo;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.domain.UserAvailability;
import com.mentoring.mentoringbackend.user.domain.UserTag;
import com.mentoring.mentoringbackend.user.dto.ContactInfoRequest;
import com.mentoring.mentoringbackend.user.dto.UserAvailabilityRequest;
import com.mentoring.mentoringbackend.user.dto.UserTagRequest;
import com.mentoring.mentoringbackend.user.repository.ContactInfoRepository;
import com.mentoring.mentoringbackend.user.repository.UserAvailabilityRepository;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import com.mentoring.mentoringbackend.user.repository.UserTagRepository;
import com.mentoring.mentoringbackend.tag.domain.Tag;
import com.mentoring.mentoringbackend.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserAvailabilityRepository userAvailabilityRepository;
    private final UserTagRepository userTagRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final TagRepository tagRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserDetails principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    // === Availability ===
    @Transactional
    public List<UserAvailability> updateMyAvailability(List<UserAvailabilityRequest> requests) {
        User me = getCurrentUser();
        userAvailabilityRepository.deleteAllByUserId(me.getId());

        List<UserAvailability> list = requests.stream()
                .map(req -> UserAvailability.builder()
                        .user(me)
                        .dayOfWeek(req.getDayOfWeek())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .mode(req.getMode())
                        .build())
                .toList();

        return userAvailabilityRepository.saveAll(list);
    }

    public List<UserAvailability> getMyAvailability() {
        User me = getCurrentUser();
        return userAvailabilityRepository.findAllByUserId(me.getId());
    }

    // === Tags ===
    @Transactional
    public List<UserTag> updateMyTags(List<UserTagRequest> requests) {
        User me = getCurrentUser();
        userTagRepository.deleteAllByUserId(me.getId());

        List<UserTag> list = requests.stream()
                .map(req -> {
                    Tag tag = tagRepository.findById(req.getTagId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다."));
                    return UserTag.builder()
                            .user(me)
                            .tag(tag)
                            .relationType(req.getRelationType())
                            .level(req.getLevel())
                            .build();
                })
                .toList();

        return userTagRepository.saveAll(list);
    }

    public List<UserTag> getMyTags() {
        User me = getCurrentUser();
        return userTagRepository.findAllByUserId(me.getId());
    }

    // === Contact Info ===
    @Transactional
    public List<ContactInfo> updateMyContactInfos(List<ContactInfoRequest> requests) {
        User me = getCurrentUser();
        contactInfoRepository.deleteAllByUserId(me.getId());

        List<ContactInfo> list = requests.stream()
                .map(req -> ContactInfo.builder()
                        .user(me)
                        .type(req.getType())
                        .value(req.getValue())
                        .primary(req.isPrimary())
                        .visibleToWorkspaceMembers(req.isVisibleToWorkspaceMembers())
                        .build())
                .toList();

        return contactInfoRepository.saveAll(list);
    }

    public List<ContactInfo> getMyContactInfos() {
        User me = getCurrentUser();
        return contactInfoRepository.findAllByUserId(me.getId());
    }
}
