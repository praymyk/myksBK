package com.myks.myksbk.domain.ticket.repository;

import com.myks.myksbk.domain.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findAllByCustomerIdOrderBySubmittedAtDescIdDesc(Long customerId);

}