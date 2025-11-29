package com.mentoring.mentoringbackend.academic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "major")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
