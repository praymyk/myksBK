package com.myks.myksbk.domain.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class) // 날짜 자동 주입 리스너
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_id")
    private Long companyId;

    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status; // Enum으로 관리 추천

    @CreatedDate // 생성 시 자동으로 현재 시간 주입
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}