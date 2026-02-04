package com.myks.myksbk.domain.ticket.controller;

import com.myks.myksbk.domain.ticket.domain.TicketStatus;
import com.myks.myksbk.domain.ticket.dto.TicketDto;
import com.myks.myksbk.domain.ticket.dto.TicketEventDto;
import com.myks.myksbk.domain.ticket.service.TicketEventService;
import com.myks.myksbk.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketEventService ticketEventService;

    @GetMapping("/tickets")
    public ResponseEntity<TicketDto.TicketListResponse> getTickets(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") String pageSize,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "receivedAt:desc", name = "at") String sortParam
    ) {

        int size = "all".equalsIgnoreCase(pageSize) ? 1000 : Integer.parseInt(pageSize);

        TicketDto.TicketListResponse result = ticketService.getCompanyTickets(
                companyId,
                page,
                size,
                status,
                sortParam
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketDto.TicketDetailResponse> getTicketDetail(@PathVariable Long id) {
        TicketDto.TicketDetailResponse detail = ticketService.getTicketDetail(id);
        return ResponseEntity.ok(detail);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @GetMapping("/tickets/{id}/events")
    public ResponseEntity<TicketEventDto.ListResponse> getTicketEvents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        TicketEventDto.ListResponse result = ticketEventService.getTicketEvents(id, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tickets/{id}/events")
    public ResponseEntity<Long> createTicketEvent(
            @PathVariable Long id,
            @RequestBody TicketEventDto.CreateRequest request
    ) {
        Long createdId = ticketEventService.createTicketEvent(id, request);
        return ResponseEntity.ok(createdId);
    }
}