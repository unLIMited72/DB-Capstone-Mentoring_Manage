package com.mentoring.mentoringbackend.workspace.domain;

import com.mentoring.mentoringbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(
        name = "workspace_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workspace_member",
                        columnNames = {"workspace_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workspace_member_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkspaceRole role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
