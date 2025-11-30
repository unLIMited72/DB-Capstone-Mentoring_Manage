package com.mentoring.mentoringbackend.workspace.dto;

import com.mentoring.mentoringbackend.workspace.domain.WorkspaceRole;
import com.mentoring.mentoringbackend.workspace.domain.WorkspaceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkspaceDetailResponse {

    private Long id;
    private String title;
    private String description;

    private Long programId;
    private String programName;

    private WorkspaceStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Member> members;

    @Data
    @Builder
    public static class Member {
        private Long userId;
        private String name;
        private WorkspaceRole role;
        private LocalDateTime joinedAt;

        // 🔹 이 워크스페이스에서 볼 수 있는 연락처 목록
        private List<Contact> contacts;
    }

    @Data
    @Builder
    public static class Contact {
        private String type;   // EMAIL / KAKAO / DISCORD / PHONE / ...
        private String value;  // 실제 주소/ID/번호
        private boolean primary;
    }
}
