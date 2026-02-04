package com.myks.myksbk.domain.ticket.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketEventChannel {
    CALL("전화"),
    CHAT("채팅"),
    EMAIL("이메일"),
    ETC("기타");

    private final String label;
}