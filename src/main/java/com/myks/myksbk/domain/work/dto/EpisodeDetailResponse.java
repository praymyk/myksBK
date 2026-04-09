package com.myks.myksbk.domain.work.dto;

import com.myks.myksbk.domain.work.domain.EpisodeStatus;
import java.time.LocalDateTime;
import java.util.List;

public record EpisodeDetailResponse(
        Long id,
        Long workId,
        Integer episodeNo,
        String title,
        String body,
        List<String> paragraphs,
        List<AnchorDto> anchors,
        EpisodeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
