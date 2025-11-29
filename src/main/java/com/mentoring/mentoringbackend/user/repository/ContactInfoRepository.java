package com.mentoring.mentoringbackend.user.repository;

import com.mentoring.mentoringbackend.user.domain.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    List<ContactInfo> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
