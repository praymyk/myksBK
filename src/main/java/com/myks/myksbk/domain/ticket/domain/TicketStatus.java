package com.myks.myksbk.domain.ticket.domain;

public enum TicketStatus {
    OPEN("접수"),
    IN_PROGRESS("진행중"),
    DONE("종료"),
    CANCELED("취소"); // 영어 코드로 정의

    private final String label;
    TicketStatus(String label) { this.label = label; }
}