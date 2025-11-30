package com.mentoring.mentoringbackend.user.domain;

import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "contact_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔹 새로 추가: 어떤 워크스페이스 전용 연락처인지 (null이면 글로벌)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ContactType type;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "visible_to_workspace_members", nullable = false)
    private boolean visibleToWorkspaceMembers;

    public void changeVisibleToWorkspaceMembers(boolean visible) {
        this.visibleToWorkspaceMembers = visible;
    }
}
