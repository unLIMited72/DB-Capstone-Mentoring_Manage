package com.mentoring.mentoringbackend.user.repository;

import com.mentoring.mentoringbackend.user.domain.ContactInfo;
import com.mentoring.mentoringbackend.user.domain.ContactType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    // 🔹 글로벌 연락처만 조회 (Workspace에 종속되지 않은 것)
    List<ContactInfo> findAllByUserIdAndWorkspaceIsNull(Long userId);

    // 🔹 글로벌 연락처만 삭제
    void deleteAllByUserIdAndWorkspaceIsNull(Long userId);

    // === 우리가 추가했던 것들 ===

    // 특정 워크스페이스에서 보일 수 있는 연락처만 조회
    @Query("""
        select c from ContactInfo c
        where c.user.id = :userId
          and (
                (c.workspace is null and c.visibleToWorkspaceMembers = true)
             or (c.workspace.id = :workspaceId)
          )
        """)
    List<ContactInfo> findVisibleForWorkspace(Long userId, Long workspaceId);

    // 워크스페이스 전용 이메일이 존재하는지 확인
    boolean existsByUserIdAndTypeAndWorkspace_Id(Long userId, ContactType type, Long workspaceId);

    // (선택) 글로벌 이메일 존재 여부
    boolean existsByUserIdAndTypeAndWorkspaceIsNull(Long userId, ContactType type);
}

