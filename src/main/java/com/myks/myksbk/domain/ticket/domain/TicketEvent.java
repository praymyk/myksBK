package com.myks.myksbk.domain.ticket.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ticket_events")
@EntityListeners(AuditingEntityListener.class)
public class TicketEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "company_id")
    private Long companyId;

    @Enumerated(EnumType.STRING) // DB에 문자열로 저장 ('문의접수' 등)
    @Column(name = "event_type")
    private TicketEventType eventType;

    @Enumerated(EnumType.STRING)
    private TicketEventChannel channel;

    @Column(name = "author_user_id")
    private Long authorUserId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "json")
    private String meta;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}