package com.mentoring.mentoringbackend.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactType type;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary;

    @Column(name = "visible_to_workspace_members", nullable = false)
    private Boolean visibleToWorkspaceMembers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
