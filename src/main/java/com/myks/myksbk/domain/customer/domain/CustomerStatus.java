package com.myks.myksbk.domain.customer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String label;
}