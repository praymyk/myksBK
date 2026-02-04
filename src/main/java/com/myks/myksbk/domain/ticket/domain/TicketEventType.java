package com.myks.myksbk.domain.ticket.domain;

public enum TicketEventType {
    CREATED("문의접수"),
    LOG("상담기록"),
    NOTE_AGENT("상담사메모"),
    NOTE_CUSTOMER("고객메모"),
    STATUS_CHANGED("상태변경"),
    MERGED("티켓병합"),
    SPLIT("티켓분리"),
    SYSTEM("시스템");

    private final String label;
    TicketEventType(String label) { this.label = label; }
}