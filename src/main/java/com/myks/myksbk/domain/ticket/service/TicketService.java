package com.myks.myksbk.domain.ticket.service;

import com.myks.myksbk.domain.ticket.domain.Ticket;
import com.myks.myksbk.domain.ticket.domain.TicketStatus;
import com.myks.myksbk.domain.ticket.dto.TicketDto;
import com.myks.myksbk.domain.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    /**
     * @param status String 대신 TicketStatus Enum으로 바로 받습니다.
     * (Controller에서 Spring이 자동으로 변환해줍니다.)
     */
    public TicketDto.TicketListResponse getCompanyTickets(
            Long companyId,
            int page,
            int pageSize,
            TicketStatus status,
            String sortParam
    ) {
        // 1. 정렬 조건 매핑
        Sort sort = createSort(sortParam);

        // 2. 페이징 객체 생성
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, sort);

        // 3. 검색 조건 (Specification)
        Specification<Ticket> spec = (root, query, cb) -> {
            // WHERE company_id = ?
            var predicate = cb.equal(root.get("companyId"), companyId);

            // AND status = ?
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };

        // 4. DB 조회
        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);

        // 5. DTO 변환
        List<TicketDto.TicketResponse> rows = ticketPage.getContent().stream()
                .map(t -> TicketDto.TicketResponse.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .description(t.getDescription())
                        .assigneeId(t.getAssigneeId())
                        .status(t.getStatus())
                        .submittedAt(t.getSubmittedAt())
                        .closedAt(t.getClosedAt())
                        .build())
                .toList();

        return TicketDto.TicketListResponse.builder()
                .rows(rows)
                .total(ticketPage.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    // (정렬 로직은 그대로 유지)
    private Sort createSort(String sortParam) {
        if (!StringUtils.hasText(sortParam)) {
            return Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id"));
        }
        switch (sortParam) {
            case "receivedAt:asc":
                return Sort.by(Sort.Order.asc("submittedAt"), Sort.Order.asc("id"));
            case "processedAt:desc":
                return Sort.by(Sort.Order.desc("closedAt"), Sort.Order.desc("id"));
            case "processedAt:asc":
                return Sort.by(Sort.Order.asc("closedAt"), Sort.Order.asc("id"));
            case "receivedAt:desc":
            default:
                return Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id"));
        }
    }

    public TicketDto.TicketDetailResponse getTicketDetail(Long id) {
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 티켓을 찾을 수 없습니다."));

        return TicketDto.TicketDetailResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .companyId(t.getCompanyId())
                .customerId(t.getCustomerId())
                .assigneeId(t.getAssigneeId())
                .channel(t.getChannel())
                .submittedAt(t.getSubmittedAt())
                .closedAt(t.getClosedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }


    public List<TicketDto.CustomerTicketResponse> getCustomerTickets(Long customerId) {
        List<Ticket> tickets = ticketRepository.findAllByCustomerIdOrderBySubmittedAtDescIdDesc(customerId);

        return tickets.stream()
                .map(ticket -> TicketDto.CustomerTicketResponse.builder()
                        .id(ticket.getId())
                        .assigneeId(ticket.getAssigneeId())
                        .submittedAt(ticket.getSubmittedAt())
                        .title(ticket.getTitle())
                        .description(ticket.getDescription())
                        .status(ticket.getStatus())
                        .createdAt(ticket.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}