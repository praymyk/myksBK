package com.myks.myksbk.domain.ticket.dto;

import com.myks.myksbk.domain.ticket.domain.TicketEventChannel;
import com.myks.myksbk.domain.ticket.domain.TicketStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDto {

    @Builder
    public record TicketResponse(
            Long id,
            String title,
            String description,
            Long assigneeId,
            TicketStatus status,
            LocalDateTime submittedAt,
            LocalDateTime closedAt
    ) {}

    @Builder
    public record TicketListResponse(
            List<TicketResponse> rows,
            long total,
            int page,
            int pageSize
    ) {}

    @Builder
    public record TicketDetailResponse(
            Long id,
            String title,
            String description,
            TicketStatus status,

            Long companyId,
            Long customerId,
            Long assigneeId,

            TicketEventChannel channel,
            LocalDateTime submittedAt,
            LocalDateTime closedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    @Builder
    public record CustomerTicketResponse(
            Long id,
            Long assigneeId,
            LocalDateTime submittedAt,
            String title,
            String description,
            TicketStatus status,
            LocalDateTime createdAt
    ) {}
}