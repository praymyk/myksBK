package com.myks.myksbk.domain.work.dto;

import com.myks.myksbk.domain.work.domain.WorkMode;
import com.myks.myksbk.domain.work.domain.WorkStatus;

import java.time.LocalDateTime;

public record WorkSummaryResponse(
        Long id,
        String title,
        WorkMode mode,
        WorkStatus status,
        String thumbnailUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}