package com.myks.myksbk.domain.customer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatus {
    active("활성"),
    inactive("비활성");

    private final String label;
}