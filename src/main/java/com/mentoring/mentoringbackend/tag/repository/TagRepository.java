package com.mentoring.mentoringbackend.tag.repository;

import com.mentoring.mentoringbackend.tag.domain.Tag;
import com.mentoring.mentoringbackend.tag.domain.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByType(TagType type);

    List<Tag> findAllBySystemTrue();

    List<Tag> findAllByMatchableTrue();
}
