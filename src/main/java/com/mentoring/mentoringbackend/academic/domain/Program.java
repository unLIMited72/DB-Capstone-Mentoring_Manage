package com.mentoring.mentoringbackend.academic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "program")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ProgramType type;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
