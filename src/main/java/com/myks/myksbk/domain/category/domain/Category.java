package com.myks.myksbk.domain.category.domain;

import com.myks.myksbk.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계 - 종류
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kind_id", nullable = false)
    private CategoryKind kind;

    // 논리적 구분값 (테이블 조인 없이 ID만 저장)
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // N:1 관계 - 부모 카테고리 (자기 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer level;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false, columnDefinition = "int unsigned")
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public Category(CategoryKind kind, Long companyId, Category parent, Integer level, String name, Integer sortOrder, Boolean isActive) {
        this.kind = kind;
        this.companyId = companyId;
        this.parent = parent;
        this.level = level;
        this.name = name;
        this.sortOrder = sortOrder;
        this.isActive = isActive != null ? isActive : true;
    }

    public void update(String name, Integer level, Integer sortOrder, Boolean isActive, Category parent) {
        this.name = name;
        this.level = level;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
        this.parent = parent;
    }

    public void deactivate() {
        this.isActive = false;
    }
}