package com.myks.myksbk.domain.work.dto;

import lombok.Builder;

@Builder
public record EpisodeSummaryResponse(
        Long id,
        Integer episodeNo,
        String title,
        String status
) {
}