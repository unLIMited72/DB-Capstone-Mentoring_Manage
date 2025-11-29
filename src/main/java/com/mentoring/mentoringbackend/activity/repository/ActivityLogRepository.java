package com.mentoring.mentoringbackend.activity.repository;

import com.mentoring.mentoringbackend.activity.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // 최근 로그 상위 N개 (운영자 대시보드용)
    List<ActivityLog> findTop100ByOrderByCreatedAtDesc();

    // 특정 사용자 최근 로그 상위 N개
    List<ActivityLog> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
}
