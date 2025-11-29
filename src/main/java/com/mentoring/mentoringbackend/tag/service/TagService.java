package com.mentoring.mentoringbackend.tag.service;

import com.mentoring.mentoringbackend.common.exception.BusinessException;
import com.mentoring.mentoringbackend.common.exception.ErrorCode;
import com.mentoring.mentoringbackend.tag.domain.Tag;
import com.mentoring.mentoringbackend.tag.domain.TagType;
import com.mentoring.mentoringbackend.tag.dto.TagCreateRequest;
import com.mentoring.mentoringbackend.tag.dto.TagDto;
import com.mentoring.mentoringbackend.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public TagDto createTag(TagCreateRequest request) {
        Tag parent = null;
        if (request.getParentTagId() != null) {
            parent = tagRepository.findById(request.getParentTagId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부모 태그를 찾을 수 없습니다."));
        }

        Tag tag = Tag.builder()
                .name(request.getName())
                .type(request.getType())
                .system(request.isSystem())
                .matchable(request.isMatchable())
                .parentTag(parent)
                .description(request.getDescription())
                .build();

        Tag saved = tagRepository.save(tag);
        return toDto(saved);
    }

    @Transactional
    public TagDto createCustomTag(TagCreateRequest request) {
        Tag parent = null;
        if (request.getParentTagId() != null) {
            parent = tagRepository.findById(request.getParentTagId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            "부모 태그를 찾을 수 없습니다."
                    ));
        }

        // 🔹 학생이 만드는 태그는 항상 표시용 전용:
        //    - is_system = false
        //    - is_matchable = false
        Tag tag = Tag.builder()
                .name(request.getName())
                .type(request.getType())
                .system(false)
                .matchable(false)
                .parentTag(parent)
                .description(request.getDescription())
                .build();

        Tag saved = tagRepository.save(tag);
        return toDto(saved);
    }

    public List<TagDto> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<TagDto> getTagsByType(TagType type) {
        return tagRepository.findAllByType(type)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<TagDto> getMatchableTags() {
        return tagRepository.findAllByMatchableTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private TagDto toDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .type(tag.getType())
                .system(Boolean.TRUE.equals(tag.getSystem()))
                .matchable(Boolean.TRUE.equals(tag.getMatchable()))
                .parentTagId(tag.getParentTag() != null ? tag.getParentTag().getId() : null)
                .parentTagName(tag.getParentTag() != null ? tag.getParentTag().getName() : null)
                .description(tag.getDescription())
                .build();
    }
}
