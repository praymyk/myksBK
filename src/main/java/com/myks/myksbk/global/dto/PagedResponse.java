package com.myks.myksbk.global.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {
    private List<T> rows;
    private long total;
    private int page;      // 1-based
    private int pageSize;

    public static <T> PagedResponse<T> from(Page<T> pageData) {
        return PagedResponse.<T>builder()
                .rows(pageData.getContent())
                .total(pageData.getTotalElements())
                .page(pageData.getNumber() + 1)      // Spring은 0-based
                .pageSize(pageData.getSize())
                .build();
    }
}