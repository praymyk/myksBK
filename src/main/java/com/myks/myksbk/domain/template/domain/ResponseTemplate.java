package com.myks.myksbk.domain.template.domain;

import com.myks.myksbk.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "response_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // DB: VARCHAR(20)
    @Column(nullable = false, length = 20)
    private String kind;

    // DB: VARCHAR(200)
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ResponseTemplate(Long companyId, String kind, String title, String prompt, String content, Long createdBy) {
        this.companyId = companyId;
        this.kind = kind;
        this.title = title;
        this.prompt = prompt;
        this.content = content;
        this.createdBy = createdBy;
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}