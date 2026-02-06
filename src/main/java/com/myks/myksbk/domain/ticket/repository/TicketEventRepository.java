package com.myks.myksbk.domain.ticket.repository;

import com.myks.myksbk.domain.ticket.domain.TicketEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    @Query("""
        SELECT e 
        FROM TicketEvent e 
        JOIN Ticket t ON e.ticketId = t.id 
        WHERE t.id = :ticketId OR t.mergedIntoTicket = :ticketId 
        ORDER BY e.createdAt DESC, e.id DESC
    """)
    Page<TicketEvent> findEventsByTicketCluster(@Param("ticketId") Long ticketId, Pageable pageable);
}