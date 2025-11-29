package com.mentoring.mentoringbackend.assignment.domain;

import com.mentoring.mentoringbackend.session.domain.Session;
import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.workspace.domain.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "assignment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "session_id")
    private Session session; // NULL 가능

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === 편의 메서드 ===
    public void update(String title,
                       String description,
                       LocalDateTime dueDate,
                       Session session) {

        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (dueDate != null) this.dueDate = dueDate;
        if (session != null) this.session = session;
    }
}
