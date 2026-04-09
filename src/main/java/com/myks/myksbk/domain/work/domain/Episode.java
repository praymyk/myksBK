package com.myks.myksbk.domain.work.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "work_episodes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_episode_work_no", columnNames = {"work_id", "episode_no"})
        },
        indexes = {
                @Index(name = "idx_episode_work", columnList = "work_id"),
                @Index(name = "idx_episode_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "episode_no", nullable = false)
    private Integer episodeNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Column(name = "paragraphs_json", columnDefinition = "JSON")
    private String paragraphsJson;

    @Column(name = "anchors_json", columnDefinition = "JSON")
    private String anchorsJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EpisodeStatus status = EpisodeStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}