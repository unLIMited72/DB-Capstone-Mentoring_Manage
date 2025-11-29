package com.mentoring.mentoringbackend.tag.domain;

import com.mentoring.mentoringbackend.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "post_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_tag",
                        columnNames = {"post_id", "tag_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
