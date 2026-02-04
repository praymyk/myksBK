package com.myks.myksbk.domain.ticket.controller;

import com.myks.myksbk.domain.ticket.dto.TicketDto;
import com.myks.myksbk.domain.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/common/customers")
@RequiredArgsConstructor
public class CustomerTicketController {

    private final TicketService ticketService;

    @GetMapping("/{customerId}/tickets")
    public ResponseEntity<List<TicketDto.CustomerTicketResponse>> getCustomerTickets(
            @PathVariable Long customerId
    ) {
        List<TicketDto.CustomerTicketResponse> result = ticketService.getCustomerTickets(customerId);
        return ResponseEntity.ok(result);
    }
}