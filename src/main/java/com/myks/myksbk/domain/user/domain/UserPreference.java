package com.myks.myksbk.domain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: user_id (User 테이블과 1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "dark_mode", nullable = false)
    private boolean darkMode; // 0: false, 1: true

    @Column(name = "default_page_size", nullable = false)
    private int defaultPageSize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserPreference(User user, boolean darkMode, int defaultPageSize) {
        this.user = user;
        this.darkMode = darkMode;
        this.defaultPageSize = defaultPageSize;
    }

    // 데이터 수정용 편의 메서드 (Upsert 시 사용)
    public void update(boolean darkMode, int defaultPageSize) {
        this.darkMode = darkMode;
        this.defaultPageSize = defaultPageSize;
    }
}