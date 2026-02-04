package com.myks.myksbk.domain.ticket.service;
import com.myks.myksbk.domain.ticket.domain.Ticket;
import com.myks.myksbk.domain.ticket.domain.TicketEvent;
import com.myks.myksbk.domain.ticket.dto.TicketEventDto;
import com.myks.myksbk.domain.ticket.repository.TicketEventRepository;
import com.myks.myksbk.domain.ticket.repository.TicketRepository;
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
    private final TicketRepository ticketRepository;

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

    @Transactional
    public Long createTicketEvent(Long ticketId, TicketEventDto.CreateRequest request) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 티켓입니다."));

        TicketEvent event = TicketEvent.builder()
                .ticketId(ticket.getId())
                .companyId(ticket.getCompanyId())
                .eventType(request.eventType())
                .channel(request.channel())
                .authorUserId(request.authorUserId())
                .customerId(request.customerId())
                .content(request.content())
                .meta(request.meta())
                .build();

        return ticketEventRepository.save(event).getId();
    }
}

