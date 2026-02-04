package com.myks.myksbk.domain.ticket.dto;

import com.myks.myksbk.domain.ticket.domain.TicketEventChannel;
import com.myks.myksbk.domain.ticket.domain.TicketEventType;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

public class TicketEventDto {

    @Builder
    public record CreateRequest(
            Long ticketId,
            Long companyId,
            TicketEventType eventType,
            TicketEventChannel channel,
            Long authorUserId,
            Long customerId,
            String content,
            String meta
    ) {}

    // 단건 조회 (TicketEventRow 대응)
    @Builder
    public record Response(
            Long id,
            Long ticketId,
            Long companyId,
            TicketEventType eventType,
            TicketEventChannel channel,
            Long authorUserId,
            Long customerId,
            String content,
            String meta,
            LocalDateTime createdAt
    ) {}

    @Builder
    public record ListResponse(
            List<Response> rows,
            long total,
            int page,
            int pageSize
    ) {}
}