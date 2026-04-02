package com.myks.myksbk.domain.work.dto;

import com.myks.myksbk.domain.work.domain.EpisodeStatus;

public record EpisodeSaveRequest(
        Integer episodeNo, // 프론트엔드에서 계산한 회차 번호 (서버에서 보정됨)
        String title,
        String rawText,
        EpisodeStatus status
) {
}
