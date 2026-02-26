package com.myks.myksbk.domain.user.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, unique = true)
    private String account;

    @Column(name = "public_id")
    private String publicId;

    @Column(nullable = false)
    private String name;

    @Column(name = "profile_name")
    private String profileName;

    @Column(nullable = false, unique = true)
    private String email;

    private String extension;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    // { 로그아웃이 필요한 이벤트 발생시 올리기 }
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Builder
    public User(Long companyId, String account, String name, String email, String passwordHash, UserStatus status) {
        this.companyId = companyId;
        this.account = account;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.tokenVersion = 0;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void bumpTokenVersion() {
        this.tokenVersion++;
        this.lastLogoutAt = LocalDateTime.now();
    }
}