package com.myks.myksbk.domain.work.dto;

public record AnchorDto(
        String id,
        Integer afterParagraphIndex,
        String source,
        String caption
) {
}
