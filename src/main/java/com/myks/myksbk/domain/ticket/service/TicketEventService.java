package com.myks.myksbk.domain.ticket.service;
import com.myks.myksbk.domain.ticket.domain.TicketEvent;
import com.myks.myksbk.domain.ticket.dto.TicketEventDto;
import com.myks.myksbk.domain.ticket.repository.TicketEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketEventService {

    private final TicketEventRepository ticketEventRepository;

    public TicketEventDto.ListResponse getTicketEvents(Long ticketId, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(Math.max(0, page - 1), pageSize);

        Page<TicketEvent> eventPage = ticketEventRepository.findEventsByTicketCluster(ticketId, pageable);

        List<TicketEventDto.Response> rows = eventPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return TicketEventDto.ListResponse.builder()
                .rows(rows)
                .total(eventPage.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    @Transactional
    public Long createTicketEvent(TicketEventDto.CreateRequest request) {
        TicketEvent event = TicketEvent.builder()
                .ticketId(request.ticketId())
                .companyId(request.companyId())
                .eventType(request.eventType())
                .channel(request.channel())
                .authorUserId(request.authorUserId())
                .customerId(request.customerId())
                .content(request.content())
                .meta(request.meta())
                .build();

        return ticketEventRepository.save(event).getId();
    }

    private TicketEventDto.Response toResponse(TicketEvent e) {
        return TicketEventDto.Response.builder()
                .id(e.getId())
                .ticketId(e.getTicketId())
                .companyId(e.getCompanyId())
                .eventType(e.getEventType())
                .channel(e.getChannel())
                .authorUserId(e.getAuthorUserId())
                .customerId(e.getCustomerId())
                .content(e.getContent())
                .meta(e.getMeta())
                .createdAt(e.getCreatedAt())
                .build();
    }
}