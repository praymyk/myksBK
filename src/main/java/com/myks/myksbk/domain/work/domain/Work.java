package com.myks.myksbk.domain.work.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "works",
        indexes = {
                @Index(name = "idx_works_company", columnList = "company_id"),
                @Index(name = "idx_works_author", columnList = "author_user_id"),
                @Index(name = "idx_works_status", columnList = "status"),
                @Index(name = "idx_works_mode", columnList = "mode")
        })
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="company_id", nullable = false)
    private Long companyId;

    @Column(name="author_user_id", nullable = false)
    private Long authorUserId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name="thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name="thumbnail_key", length = 255)
    private String thumbnailKey;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private WorkMode mode;


    @Column(name="ai_image_enabled", nullable = false)
    private Boolean aiImageEnabled;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private WorkStatus status;

    @Column(name="tags_json", columnDefinition = "JSON")
    private String tagsJson;

    @Column(name="created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}